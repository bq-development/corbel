package io.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import io.corbel.resources.rem.service.ResmiService;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        ResourceUri resourceUri = buildRelationUri(type, id.getId(), relation, parameters.getOptionalApiParameters().flatMap(params -> params.getPredicateResource()));

        try {
            JsonObject requestEntity = entity.orElse(null);
            resmiService.createRelation(resourceUri, requestEntity);
            return created();
        } catch (NotFoundException | NullPointerException | IllegalArgumentException e) {
            return ErrorResponseFactory.getInstance().badRequest(
                    new Error("bad_request", e.getClass().getSimpleName() + ": " + e.getMessage()));
        } catch (StartsWithUnderscoreException e) {
            return ErrorResponseFactory.getInstance().invalidEntity("Invalid attribute name \"" + e.getMessage() + "\"");
        }
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
            return UriBuilder.fromUri(typeUri).path("/{id}").build(idElement.getAsString());
        }
        return null;
    }

}
