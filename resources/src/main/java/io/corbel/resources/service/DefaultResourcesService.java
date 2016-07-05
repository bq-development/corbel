package io.corbel.resources.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.Lists;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.service.EventBus;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.parser.QueryParametersParser;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ApiRequestException;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.rem.internal.RemEntityTypeResolver;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.RemService;

/**
 * Created by Alexander De Leon on 26/05/15.
 */
public class DefaultResourcesService implements ResourcesService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourcesService.class);
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[] {};
    private static final String UNABLE_TO_READ_ERROR = "Unable to read entity from content of media type {}";
    private final RemService remService;
    private final RemEntityTypeResolver remEntityTypeResolver;
    private final int defaultPageSize;
    private final int maxPageSize;

    private final EventBus eventBus;
    private final QueryParametersParser queryParametersParser;


    private Providers providers;

    public DefaultResourcesService(RemService remService, RemEntityTypeResolver remEntityTypeResolver, int defaultPageSize,
            int maxPageSize, QueryParametersParser queryParametersBuilder, EventBus eventBus) {
        this.remService = remService;
        this.remEntityTypeResolver = remEntityTypeResolver;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
        this.queryParametersParser = queryParametersBuilder;
        this.eventBus = eventBus;
    }

    @Override
    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    @Override
    public Response collectionOperation(String domain, String type, Request request, UriInfo uriInfo, TokenInfo tokenInfo, URI typeUri,
            HttpMethod method, QueryParameters queryParameters, InputStream inputStream, MediaType contentType) {
        Response result;
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, method);

            queryParameters = (method.equals(HttpMethod.GET) || method.equals(HttpMethod.DELETE) || method.equals(HttpMethod.PUT)) ? queryParameters
                    : getDefaultQueryParameters();
            RequestParameters<CollectionParameters> parameters = collectionParameters(domain, queryParameters, tokenInfo,
                    acceptedMediaTypes,
                    uriInfo.getQueryParameters(), request);

            Optional<?> entity = method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional
                    .empty();

            result = remService.collection(rem, type, parameters, typeUri, entity);
        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error(UNABLE_TO_READ_ERROR, contentType, e);
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
    public Response resourceOperation(String domain, String type, ResourceId id, Request request, QueryParameters queryParameters,
            UriInfo uriInfo, TokenInfo tokenInfo, URI typeUri, HttpMethod method, InputStream inputStream, MediaType contentType,
            Long contentLength) {

        if (id.isWildcard()) {
            return collectionOperation(domain, type, request, uriInfo, tokenInfo, typeUri, method, queryParameters, inputStream,
                    contentType);
        }

        Response result;
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, method);

            RequestParameters<ResourceParameters> resourceParameters = resourceParameters(domain, queryParameters, tokenInfo,
                    acceptedMediaTypes,
                    contentLength, uriInfo.getQueryParameters(), request);
            Optional<?> entity = method == HttpMethod.PUT ? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional
                    .empty();

            result = remService.resource(rem, type, id, resourceParameters, entity);

        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error(UNABLE_TO_READ_ERROR, contentType, e);
            result = ErrorResponseFactory.getInstance().serverError(e);
        } catch (ApiRequestException e) {
            result = ErrorResponseFactory.getInstance().badRequest(e);
        }

        sendEvent(method, tokenInfo, result, HttpStatus.NO_CONTENT_204, type, id.getId(), method == HttpMethod.PUT);
        return result;
    }

    @Override
    public Response relationOperation(String domain, String type, ResourceId id, String rel, Request request, UriInfo uriInfo,
            TokenInfo tokenInfo, HttpMethod method, QueryParameters queryParameters, String resource, InputStream inputStream,
            MediaType contentType) {

        Response result;
        String relationId;
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type + "/" + id.getId() + "/" + rel, acceptedMediaTypes, method);

            queryParameters = Optional.ofNullable(queryParameters).orElse(getDefaultQueryParameters());
            RequestParameters<RelationParameters> parameters = relationParameters(domain, queryParameters, Optional.ofNullable(resource),
                    tokenInfo, acceptedMediaTypes, uriInfo.getQueryParameters(), request);
            Optional<?> entity = getEntity(Optional.ofNullable(inputStream), rem, contentType);

            result = remService.relation(rem, type, id, rel, parameters, entity);
            relationId = parameters.getOptionalApiParameters().flatMap(RelationParameters::getPredicateResource).orElse(null);

        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error(UNABLE_TO_READ_ERROR, contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        } catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }

        sendEvent(method, tokenInfo, result, HttpStatus.CREATED_201, type + "/" + id.getId() + "/" + rel, relationId, method != HttpMethod.DELETE);
        return result;
    }

    private void sendEvent(HttpMethod method, TokenInfo tokenInfo, Response result, int expectedStatus, String type, String id, boolean updateCondition) {
        if (method != HttpMethod.GET
                && tokenInfo != null
                && (result.getStatus() == expectedStatus || result.getStatus() == HttpStatus.OK_200)) {
            ResourceEvent event;
            if (updateCondition) {
                event = ResourceEvent.updateResourceEvent(type, id, tokenInfo.getDomainId(), tokenInfo.getUserId());
            } else {
                event = ResourceEvent.deleteResourceEvent(type, id, tokenInfo.getDomainId(), tokenInfo.getUserId());
            }
            eventBus.dispatch(event);
        }
    }

    private RequestParameters<CollectionParameters> collectionParameters(String domain, QueryParameters queryParameters,
            TokenInfo tokenInfo,
            List<org.springframework.http.MediaType> acceptedMediaTypes, MultivaluedMap<String, String> params, Request request) {
        return new RequestParametersImpl<>(new CollectionParametersImpl(queryParameters), tokenInfo, domain, acceptedMediaTypes, null,
                params,
                getHeadersFromRequest(request));
    }

    private RequestParameters<ResourceParameters> resourceParameters(String domain, QueryParameters queryParameters, TokenInfo tokenInfo,
            List<org.springframework.http.MediaType> acceptedMediaTypes, Long contentLength, MultivaluedMap<String, String> params,
            Request request) {
        return new RequestParametersImpl<>(new ResourceParametersImpl(queryParameters), tokenInfo, domain, acceptedMediaTypes,
                contentLength,
                params, getHeadersFromRequest(request));
    }

    private RequestParameters<RelationParameters> relationParameters(String domain, QueryParameters queryParameters,
            Optional<String> predicateResourceUri, TokenInfo tokenInfo, List<org.springframework.http.MediaType> acceptedMediaTypes,
            MultivaluedMap<String, String> params, Request request) {
        return new RequestParametersImpl<>(new RelationParametersImpl(queryParameters, predicateResourceUri), tokenInfo, domain,
                acceptedMediaTypes, null, params, getHeadersFromRequest(request));
    }

    private QueryParameters getDefaultQueryParameters() {
        return queryParametersParser.createQueryParameters(0, defaultPageSize, maxPageSize, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }


    private List<org.springframework.http.MediaType> getRequestAcceptedMediaTypes(Request request) {
        ContainerRequest containerRequest = (ContainerRequest) request;
        return Lists.transform(containerRequest.getAcceptableMediaTypes(), input -> new org.springframework.http.MediaType(input.getType(),
                input.getSubtype(), input.getParameters()));
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
