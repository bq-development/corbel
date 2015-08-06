package io.corbel.resources.rem.resmi;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.resources.rem.dao.JsonRelation;
import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.dao.RelationMoveOperation;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import io.corbel.resources.rem.service.ResmiService;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public class ResmiPutRem extends AbstractResmiRem {

    public ResmiPutRem(ResmiService resmiService) {
        super(resmiService);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<JsonObject> entity) {
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildResourceUri(type, id.getId());
        return entity.map(object -> {
            try {
                Optional<List<ResourceQuery>> conditions = Optional.ofNullable(parameters)
                        .flatMap(RequestParameters::getOptionalApiParameters)
                        .map(ResourceParameters::getConditions)
                        .orElse(Optional.empty());
                if (conditions.isPresent()) {
                    JsonObject result = resmiService.conditionalUpdateResource(resourceUri, object, conditions.get());
                    if (result == null) {
                                return ErrorResponseFactory.getInstance().preconditionFailed("Condition not satisfied.");
                    }
                } else {
                    resmiService.updateResource(resourceUri, object);
                }
            } catch (StartsWithUnderscoreException e) {
                return ErrorResponseFactory.getInstance().invalidEntity("Invalid attribute name \"" + e.getMessage() + "\"");
            }

            return noContent();
        }).orElse(ErrorResponseFactory.getInstance().badRequest());
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildRelationUri(type, id.getId(), relation, parameters.getOptionalApiParameters().flatMap(params -> params.getPredicateResource()));

        if (id.isWildcard()) {
            ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (parameters.getOptionalApiParameters().flatMap(params -> params.getPredicateResource()).isPresent()) {
            try {
                String uri = URLDecoder.decode(parameters.getOptionalApiParameters().get().getPredicateResource().get(), "UTF-8");
                if (JsonRelation.validateUri(uri)) {

                    JsonObject requestEntity = entity.orElse(null);
                    if (requestEntity != null && requestEntity.has("_order")) {
                        String operation = requestEntity.get("_order").getAsString();
                        resmiService.moveRelation(resourceUri, RelationMoveOperation.create(operation));
                        return noContent();
                    } else {
                        resmiService.createRelation(resourceUri, requestEntity);
                        return created();
                    }
                }
            } catch (NotFoundException | UnsupportedEncodingException | NullPointerException | IllegalArgumentException e) {
                return ErrorResponseFactory.getInstance().badRequest(
                        new Error("bad_request", e.getClass().getSimpleName() + ": " + e.getMessage()));
            } catch (StartsWithUnderscoreException e) {
                return ErrorResponseFactory.getInstance().invalidEntity("Invalid attribute name \"" + e.getMessage() + "\"");
            }
        }
        return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", "Resource URI not present"));
    }
}
