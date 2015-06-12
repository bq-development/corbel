package com.bq.oss.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.bq.oss.corbel.resources.rem.service.ResmiService;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.uri.UriBuilderImpl;

/**
 * @author Rubén Carrasco
 */
public class ResmiPostRem extends AbstractResmiRem {

    public ResmiPostRem(ResmiService resmiService) {
        super(resmiService);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildCollectionUri(type);
        return entity.map(object -> {
            resmiService.removeObjectId(object);
            JsonObject savedObject;
            try {
                savedObject = resmiService.saveResource(resourceUri, object, getUserIdFromToken(parameters));
            } catch (StartsWithUnderscoreException e) {
                return ErrorResponseFactory.getInstance().invalidEntity("Invalid attribute name \"" + e.getMessage() + "\"");
            }
            return buildCreatedResponse(savedObject, uri);
        }).orElse(ErrorResponseFactory.getInstance().badRequest());
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }

    private Optional<String> getUserIdFromToken(RequestParameters<CollectionParameters> parameters) {
        return Optional.ofNullable(parameters).map(RequestParameters::getTokenInfo).map(TokenInfo::getUserId);
    }

    protected Response buildCreatedResponse(JsonObject response, URI uri) {
        if (response == null) {
            return ErrorResponseFactory.getInstance().notFound();
        } else {
            return created(generateResourceLink(response, uri));
        }
    }

    private URI generateResourceLink(JsonObject resource, URI typeUri) {
        JsonElement idElement = resource.get(resource.has(ResmiService.ID) ? ResmiService.ID : ResmiService._ID);
        if (idElement.isJsonPrimitive()) {
            return UriBuilderImpl.fromUri(typeUri).path("/{id}").build(idElement.getAsString());
        }
        return null;
    }

}
