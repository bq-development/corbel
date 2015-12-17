package io.corbel.resources.service;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.springframework.http.HttpMethod;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.request.ResourceId;

/**
 * Created by Francisco Sanchez on 26/05/15.
 */
public interface ResourcesService {

    void setProviders(Providers providers);

    Response collectionOperation(String domain, String type, Request request, UriInfo uriInfo, TokenInfo tokenInfo, URI typeUri,
            HttpMethod method, QueryParameters queryParameters, InputStream inputStream, MediaType contentType);

    Response resourceOperation(String domain, String type, ResourceId id, Request request, QueryParameters queryParameters, UriInfo uriInfo,
            TokenInfo tokenInfo, URI typeUri, HttpMethod method, InputStream inputStream, MediaType contentType, Long contentLength);

    Response relationOperation(String domain, String type, ResourceId id, String rel, Request request, UriInfo uriInfo, TokenInfo tokenInfo,
            HttpMethod method, QueryParameters queryParameters, String resource, InputStream inputStream, MediaType contentType);

}
