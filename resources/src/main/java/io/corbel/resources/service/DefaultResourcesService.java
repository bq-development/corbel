package io.corbel.resources.service;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.service.EventBus;
import io.corbel.lib.queries.builder.QueryParametersBuilder;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ApiRequestException;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.rem.internal.RemEntityTypeResolver;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.CollectionParametersImpl;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RelationParametersImpl;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.RequestParametersImpl;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.request.ResourceParametersImpl;
import io.corbel.resources.rem.service.RemService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.Lists;

/**
 * Created by Alexander De Leon on 26/05/15.
 */
public class DefaultResourcesService implements ResourcesService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourcesService.class);
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[] {};

    private final RemService remService;
    private final RemEntityTypeResolver remEntityTypeResolver;
    private final int defaultPageSize;
    private final int maxPageSize;

    private final EventBus eventBus;
    private final QueryParametersBuilder queryParametersBuilder;


    private Providers providers;

    public DefaultResourcesService(RemService remService, RemEntityTypeResolver remEntityTypeResolver, int defaultPageSize,
            int maxPageSize, QueryParametersBuilder queryParametersBuilder, EventBus eventBus) {
        this.remService = remService;
        this.remEntityTypeResolver = remEntityTypeResolver;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
        this.queryParametersBuilder = queryParametersBuilder;
        this.eventBus = eventBus;
    }

    @Override
    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    @Override
    public Response collectionOperation(String type, Request request, UriInfo uriInfo, TokenInfo tokenInfo, URI typeUri, HttpMethod method,
            QueryParameters queryParameters, InputStream inputStream, MediaType contentType) {
        Response result;
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));

            queryParameters = (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.PUT)) ? queryParameters
                    : getDefaultQueryParameters();
            RequestParameters<CollectionParameters> parameters = collectionParameters(queryParameters, tokenInfo, acceptedMediaTypes,
                    uriInfo.getQueryParameters(), request);

            Optional<?> entity = method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional
                    .empty();

            result = remService.collection(rem, type, parameters, typeUri, entity);
        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        } catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }

        if (method == HttpMethod.POST && tokenInfo != null && result.getMetadata().containsKey("Location")
                && (result.getStatus() == HttpStatus.CREATED_201 || result.getStatus() == org.eclipse.jetty.http.HttpStatus.OK_200)) {
            eventBus.dispatch(ResourceEvent.createResourceEvent(type, result.getMetadata().getFirst("Location").toString(),
                    tokenInfo.getDomainId(), tokenInfo.getUserId()));
        }

        return result;
    }

    @Override
    public Response resourceOperation(String type, ResourceId id, Request request, QueryParameters queryParameters, UriInfo uriInfo,
            TokenInfo tokenInfo, URI typeUri, HttpMethod method, InputStream inputStream, MediaType contentType, Long contentLength) {

        if (id.isWildcard()) {
            return collectionOperation(type, request, uriInfo, tokenInfo, typeUri, method, queryParameters, inputStream, contentType);
        }

        Response result;
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));

            RequestParameters<ResourceParameters> resourceParameters = resourceParameters(queryParameters, tokenInfo, acceptedMediaTypes,
                    contentLength, uriInfo.getQueryParameters(), request);
            Optional<?> entity = method == HttpMethod.PUT ? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional
                    .empty();

            result = remService.resource(rem, type, id, resourceParameters, entity);

        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            result = ErrorResponseFactory.getInstance().serverError(e);
        } catch (ApiRequestException e) {
            result = ErrorResponseFactory.getInstance().badRequest(e);
        }

        if (method != HttpMethod.GET
                && tokenInfo != null
                && (result.getStatus() == org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204 || result.getStatus() == org.eclipse.jetty.http.HttpStatus.OK_200)) {
            ResourceEvent event;
            if (method == HttpMethod.PUT) {
                event = ResourceEvent.updateResourceEvent(type, id.getId(), tokenInfo.getDomainId(), tokenInfo.getUserId());
            } else {
                event = ResourceEvent.deleteResourceEvent(type, id.getId(), tokenInfo.getDomainId(), tokenInfo.getUserId());
            }
            eventBus.dispatch(event);
        }
        return result;
    }

    @Override
    public Response relationOperation(String type, ResourceId id, String rel, Request request, UriInfo uriInfo, TokenInfo tokenInfo,
            HttpMethod method, QueryParameters queryParameters, String resource, InputStream inputStream, MediaType contentType) {
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type + "/" + id.getId() + "/" + rel, acceptedMediaTypes, getRequestMethod(request));

            queryParameters = method.equals(HttpMethod.GET) ? queryParameters : getDefaultQueryParameters();
            RequestParameters<RelationParameters> parameters = relationParameters(queryParameters, Optional.ofNullable(resource),
                    tokenInfo, acceptedMediaTypes, uriInfo.getQueryParameters(), request);
            Optional<?> entity = getEntity(Optional.ofNullable(inputStream), rem, contentType);

            return remService.relation(rem, type, id, rel, parameters, entity);

        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        } catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }
    }

    private RequestParameters<CollectionParameters> collectionParameters(QueryParameters queryParameters, TokenInfo tokenInfo,
            List<org.springframework.http.MediaType> acceptedMediaTypes, MultivaluedMap<String, String> params, Request request) {
        return new RequestParametersImpl<>(new CollectionParametersImpl(queryParameters), tokenInfo, acceptedMediaTypes, null, params,
                getHeadersFromRequest(request));
    }

    private RequestParameters<ResourceParameters> resourceParameters(QueryParameters queryParameters, TokenInfo tokenInfo,
            List<org.springframework.http.MediaType> acceptedMediaTypes, Long contentLength, MultivaluedMap<String, String> params,
            Request request) {
        return new RequestParametersImpl<>(new ResourceParametersImpl(queryParameters), tokenInfo, acceptedMediaTypes, contentLength,
                params, getHeadersFromRequest(request));
    }

    private RequestParameters<RelationParameters> relationParameters(QueryParameters queryParameters,
            Optional<String> predicateResourceUri, TokenInfo tokenInfo, List<org.springframework.http.MediaType> acceptedMediaTypes,
            MultivaluedMap<String, String> params, Request request) {
        return new RequestParametersImpl<>(new RelationParametersImpl(queryParameters, predicateResourceUri), tokenInfo,
                acceptedMediaTypes, null, params, getHeadersFromRequest(request));
    }

    private QueryParameters getDefaultQueryParameters() {
        return queryParametersBuilder.createQueryParameters(0, defaultPageSize, maxPageSize, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }


    private List<org.springframework.http.MediaType> getRequestAcceptedMediaTypes(Request request) {
        ContainerRequest containerRequest = (ContainerRequest) request;
        return Lists.transform(containerRequest.getAcceptableMediaTypes(), input -> new org.springframework.http.MediaType(input.getType(),
                input.getSubtype(), input.getParameters()));
    }

    private org.springframework.http.HttpMethod getRequestMethod(Request request) {
        return org.springframework.http.HttpMethod.valueOf(request.getMethod());
    }


    private MultivaluedMap<String, String> getHeadersFromRequest(Request request) {
        MultivaluedMap<String, String> headers;

        if (request instanceof ContainerRequest) {
            headers = ((ContainerRequest) request).getRequestHeaders();
        } else {
            LOG.warn("Request is not instance of ContainerRequest");
            headers = new MultivaluedHashMap<>();
        }
        return headers;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Optional<?> getEntity(Optional<InputStream> entityStream, Rem<?> rem, MediaType mediaType) throws WebApplicationException,
            IOException {
        if (entityStream.isPresent()) {
            Class type = remEntityTypeResolver.getEntityType(rem);
            MessageBodyReader<?> reader = providers.getMessageBodyReader(type, type, EMPTY_ANNOTATIONS, mediaType);
            if (reader != null) {
                return Optional.ofNullable(reader.readFrom(type, type, EMPTY_ANNOTATIONS, mediaType, null, entityStream.get()));
            }
            LOG.warn("Did not find a provider for type {} and mediaType {}", type, mediaType);
        }
        return Optional.empty();
    }


}
