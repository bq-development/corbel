package com.bq.oss.corbel.resources.service;

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

import com.bq.oss.lib.queries.builder.QueryParametersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.bq.oss.corbel.event.ResourceEvent;
import com.bq.oss.corbel.eventbus.service.EventBus;
import com.bq.oss.corbel.rem.internal.RemEntityTypeResolver;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.parser.SortParser;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.ws.api.error.ApiRequestException;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.Lists;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.server.impl.model.HttpHelper;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Created by Alexander De Leon on 26/05/15.
 */
public class DefaultResourcesService implements ResourcesService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourcesService.class);
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[]{};

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
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));

            queryParameters = method.equals(HttpMethod.GET) ? queryParameters : getDefaultQueryParameters();
            RequestParameters<CollectionParameters> parameters = collectionParameters(queryParameters, tokenInfo, acceptedMediaTypes, uriInfo.getQueryParameters(), request);

            Optional<?> entity = method.equals(HttpMethod.POST) ? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional.empty();

            return remService.collection(rem, type, parameters, typeUri, entity);
        } catch (JsonParseException e) {
            return ErrorResponseFactory.getInstance().invalidEntity(e.getOriginalMessage());
        } catch (IOException e) {
            LOG.error("Unable to read entity from content of media type {}", contentType, e);
            return ErrorResponseFactory.getInstance().serverError(e);
        } catch (ApiRequestException e) {
            return ErrorResponseFactory.getInstance().badRequest(e);
        }
    }

    @Override
    public Response resourceOperation(String type, ResourceId id, Request request, QueryParameters queryParameters, UriInfo uriInfo,
                                      TokenInfo tokenInfo, URI typeUri, HttpMethod method, InputStream inputStream, MediaType contentType, Long contentLength) {

        if (id.isWildcard()) {
            return collectionOperation(type, request, uriInfo, tokenInfo, typeUri, method, queryParameters, inputStream, contentType);
        }

        Response result = null;
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));

            RequestParameters<ResourceParameters> resourceParameters = resourceParameters(queryParameters, tokenInfo, acceptedMediaTypes, contentLength, uriInfo.getQueryParameters(),
                    request);
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

        if (tokenInfo != null
                && (result.getStatus() == org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204 || result.getStatus() == org.eclipse.jetty.http.HttpStatus.OK_200)) {
            eventBus.dispatch(ResourceEvent.updateResourceEvent(type, id.getId(), tokenInfo.getDomainId()));
        }
        return result;
    }

    @Override
    public Response relationOperation(String type, ResourceId id, String rel, Request request, UriInfo uriInfo, TokenInfo tokenInfo,
                                      HttpMethod method, QueryParameters queryParameters, String resource, InputStream inputStream, MediaType contentType) {
        try {
            List<org.springframework.http.MediaType> acceptedMediaTypes = getRequestAcceptedMediaTypes(request);
            Rem rem = remService.getRem(type, acceptedMediaTypes, getRequestMethod(request));

            queryParameters = method.equals(HttpMethod.GET) ? queryParameters : getDefaultQueryParameters();
            RequestParameters<RelationParameters> parameters = relationParameters(queryParameters, Optional.ofNullable(resource), tokenInfo, acceptedMediaTypes,
                    uriInfo.getQueryParameters(), request);
            Optional<?> entity = method.equals(HttpMethod.PUT) ? getEntity(Optional.ofNullable(inputStream), rem, contentType) : Optional.empty();

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
        return new RequestParametersImpl<>(new ResourceParametersImpl(queryParameters), tokenInfo, acceptedMediaTypes,
                contentLength, params, getHeadersFromRequest(request));
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
        return Lists.transform(HttpHelper.getAccept((HttpRequestContext) request),
                input -> new org.springframework.http.MediaType(input.getType(), input.getSubtype(), input.getParameters()));
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
