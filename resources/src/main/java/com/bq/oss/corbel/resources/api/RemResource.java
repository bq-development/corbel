package com.bq.oss.corbel.resources.api;

import com.bq.oss.corbel.resources.href.LinksFilter;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.service.ResourcesService;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.ws.annotation.Rest;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.sun.jersey.spi.container.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;
import java.io.InputStream;
import java.net.URI;

/**
 * Entry point for any resource on the resources. Here we obtain the appropiate Resource Resolver Module (REM) and delegate on it the
 * resolution of the resource's representation.
 * 
 * 
 * @author Alexander De Leon
 * 
 */
@Resource @Path(ApiVersion.CURRENT + "/resource") public class RemResource {
    private static final Logger LOG = LoggerFactory.getLogger(RemResource.class);
    private final ResourcesService resourcesService;

    public RemResource(ResourcesService resourcesService) {
        this.resourcesService = resourcesService;
    }

    // injected when the resource is registered with the container
    @Context
    public void setProviders(Providers providers) {
        resourcesService.setProviders(providers);
    }

    @GET
    @Path("/{type}")
    public Response getCollection(@PathParam("type") String type, @Context Request request, @Context UriInfo uriInfo,
            @Context AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {
        URI typeUri = getBaseUriWithType(uriInfo, type);
        updateRequestWithLinksTypeAndUri(request, typeUri, type);
        return resourcesService.collectionOperation(type, request, uriInfo, getTokenInfo(authorizationInfo), typeUri, HttpMethod.GET,
                queryParameters, null, null);
    }

    @POST
    @Path("/{type}")
    public Response postCollection(@PathParam("type") String type, @Context Request request, @Context UriInfo uriInfo,
            @Context AuthorizationInfo authorizationInfo, InputStream inputStream, @HeaderParam("Content-Type") MediaType contentType) {
        return resourcesService.collectionOperation(type, request, uriInfo, getTokenInfo(authorizationInfo),
                getBaseUriWithType(uriInfo, type), HttpMethod.POST, null, inputStream,
                contentType);
    }

    @DELETE
    @Path("/{type}")
    public Response deleteCollection(@PathParam("type") String type, @Context Request request, @Context UriInfo uriInfo,
            @Context AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {
        return resourcesService.collectionOperation(type, request, uriInfo, getTokenInfo(authorizationInfo),
                getBaseUriWithType(uriInfo, type), HttpMethod.DELETE, queryParameters, null, null);
    }

    @GET
    @Path("/{type}/{id}")
    public Response getResource(@PathParam("type") String type, @PathParam("id") ResourceId id, @Context Request request,
            @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {
        URI typeUri = getBaseUriWithType(uriInfo, type);
        updateRequestWithLinksTypeAndUri(request, typeUri, type);
        return resourcesService.resourceOperation(type, id, request, queryParameters, uriInfo, getTokenInfo(authorizationInfo), typeUri,
                HttpMethod.GET, null, null, null);
    }

    @PUT
    @Path("/{type}/{id}")
    public Response putResource(@PathParam("type") String type, @PathParam("id") ResourceId id, @Context Request request,
            @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo, InputStream inputStream,
            @HeaderParam("Content-Type") MediaType contentType, @HeaderParam("Content-Length") Long contentLength,
            @Rest QueryParameters queryParameters) {
        return resourcesService.resourceOperation(type, id, request, queryParameters, uriInfo, getTokenInfo(authorizationInfo),
                getBaseUriWithType(uriInfo, type),
                HttpMethod.PUT, inputStream, contentType, contentLength);
    }

    @DELETE
    @Path("/{type}/{id}")
    public Response deleteResource(@PathParam("type") String type, @PathParam("id") ResourceId id, @Context Request request,
            @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo, @Rest QueryParameters queryParameters) {
        return resourcesService.resourceOperation(type, id, request, queryParameters, uriInfo, getTokenInfo(authorizationInfo),
                getBaseUriWithType(uriInfo, type), HttpMethod.DELETE, null,
                null, null);
    }

    @GET
    @Path("/{type}/{id}/{rel}")
    public Response getRelation(@PathParam("type") String type, @PathParam("id") ResourceId id, @PathParam("rel") String rel,
            @Context Request request, @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo,
            @Rest QueryParameters queryParameters, @MatrixParam("r") String resource) {
        URI typeUri = getBaseUri(uriInfo);
        updateRequestWithLinksTypeAndUri(request, typeUri, type);
        return resourcesService.relationOperation(type, id, rel, request, uriInfo, getTokenInfo(authorizationInfo), HttpMethod.GET,
                queryParameters, resource, null, null);
    }

    @PUT
    @Path("/{type}/{id}/{rel}")
    public Response putRelation(@PathParam("type") String type, @PathParam("id") ResourceId id, @PathParam("rel") String rel,
            @Context Request request, @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo,
            @MatrixParam("r") String resource, InputStream inputStream, @HeaderParam("Content-Type") MediaType contentType) {
        return resourcesService.relationOperation(type, id, rel, request, uriInfo, getTokenInfo(authorizationInfo), HttpMethod.PUT, null,
                resource, inputStream, contentType);
    }

    @DELETE
    @Path("/{type}/{id}/{rel}")
    public Response deleteRelation(@PathParam("type") String type, @PathParam("id") ResourceId id, @PathParam("rel") String rel,
            @Context Request request, @Context UriInfo uriInfo, @Context AuthorizationInfo authorizationInfo,
            @MatrixParam("r") String resource) {
        return resourcesService.relationOperation(type, id, rel, request, uriInfo, getTokenInfo(authorizationInfo), HttpMethod.PUT, null,
                resource, null, null);
    }

    private void updateRequestWithLinksTypeAndUri(Request request, URI typeUri, String type) {
        try {
            ContainerRequest containerRequest = (ContainerRequest) request;
            containerRequest.getProperties().put(LinksFilter.TYPE, type);
            containerRequest.getProperties().put(LinksFilter.URI, typeUri);
        } catch (ClassCastException e) {
            LOG.error("Couldn't cast Request to ContainerRequest", e);
        }
    }

    private URI getBaseUri(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(this.getClass()).build();
    }

    private URI getBaseUriWithType(UriInfo uriInfo, String type) {
        return uriInfo.getBaseUriBuilder().path(this.getClass()).path(type).build();
    }

    private TokenInfo getTokenInfo(AuthorizationInfo authorizationInfo) {
        return authorizationInfo != null ? authorizationInfo.getTokenReader().getInfo() : null;
    }

}
