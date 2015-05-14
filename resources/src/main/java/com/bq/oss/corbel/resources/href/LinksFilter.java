package com.bq.oss.corbel.resources.href;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.resources.service.RelationSchemaService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * @author Rubén Carrasco
 * 
 */
public class LinksFilter implements ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LinksFilter.class);

    public static final String TYPE = "type";
    public static final String URI = "uri";
    private static final String GET_METHOD = "GET";

    private final LinkGenerator linkGenerator;
    private final RelationSchemaService relationSchemaService;

    public LinksFilter(LinkGenerator linkGenerator, RelationSchemaService relationSchemaService) {
        this.linkGenerator = linkGenerator;
        this.relationSchemaService = relationSchemaService;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        if (request.getMethod().equals(GET_METHOD) && request.getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getMediaType())
                && response.getStatus() == Response.Status.OK.getStatusCode()) {
            try {
                Optional.ofNullable((JsonElement) response.getEntity()).filter(object -> object.isJsonArray() || object.isJsonObject())
                        .ifPresent(object -> processRequest(request, object));
            } catch (ClassCastException e) {} catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
        return response;
    }

    private void processRequest(ContainerRequest request, JsonElement entity) {
        Optional.ofNullable(getUriWithProxyPassPath(request)).ifPresent(baseUri -> {
            String type = String.valueOf(request.getProperties().get(TYPE));
            if (type != null) {
                addLinks(entity, baseUri, Optional.ofNullable(relationSchemaService.getTypeRelations(type)));
            } else {
                addLinks(entity, baseUri, Optional.empty());
            }
        });
    }

    private java.net.URI getUriWithProxyPassPath(ContainerRequest request) {
        URI uri = (java.net.URI) request.getProperties().get("uri");
        return Optional.ofNullable(request.getHeaderValue("X-Forwarded-Uri")).map(originalUri -> {
            String proxyPassPath = originalUri.replace(request.getAbsolutePath().getPath(), "");
            try {
                return new URI(uri.getScheme(), uri.getHost(), proxyPassPath + uri.getPath(), uri.getFragment());
            } catch (URISyntaxException e) {
                LOG.error(e.getMessage());
            }
            return uri;
        }).orElse(uri);
    }

    private void addLinks(JsonElement entity, URI typeUri, Optional<Set<String>> relations) {
        if (entity.isJsonArray()) {
            for (JsonElement jsonElement : entity.getAsJsonArray()) {
                addLinks(jsonElement, typeUri, relations);
            }
        } else if (entity.isJsonObject()) {
            linkGenerator.addResourceLinks((JsonObject) entity, typeUri, relations);
        }
    }

}
