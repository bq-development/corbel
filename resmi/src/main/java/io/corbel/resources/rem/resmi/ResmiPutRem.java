package io.corbel.resources.rem.resmi;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.dao.JsonRelation;
import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.dao.RelationMoveOperation;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import io.corbel.resources.rem.service.ResmiService;

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
        ResourceUri resourceUri = buildCollectionUri(parameters.getRequestedDomain(), type);
        return entity.map(object -> {
            try {
                Optional<List<ResourceQuery>> conditions = Optional.ofNullable(parameters)
                        .flatMap(RequestParameters::getOptionalApiParameters).map(CollectionParameters::getConditions)
                        .orElse(Optional.empty());
                if (conditions.isPresent()) {
                        resmiService.updateCollection(resourceUri, object, conditions.get());
                } else {
                    resmiService.updateCollection(resourceUri, object, new ArrayList<>());
                }
            } catch (StartsWithUnderscoreException e) {
                return ErrorResponseFactory.getInstance().invalidEntity("Invalid attribute name \"" + e.getMessage() + "\"");
            }

            return noContent();
        }).orElse(ErrorResponseFactory.getInstance().badRequest());
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildResourceUri(parameters.getRequestedDomain(), type, id.getId());
        return entity.map(object -> {
            try {
                Optional<List<ResourceQuery>> conditions = Optional.ofNullable(parameters)
                        .flatMap(RequestParameters::getOptionalApiParameters).map(ResourceParameters::getConditions)
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
        ResourceUri resourceUri = buildRelationUri(parameters.getRequestedDomain(), type, id.getId(), relation,
                parameters.getOptionalApiParameters().flatMap(RelationParameters::getPredicateResource));

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (!parameters.getOptionalApiParameters().flatMap(RelationParameters::getPredicateResource).isPresent()) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", "Resource URI not present"));
        }

        try {
            String uri = URLDecoder.decode(parameters.getOptionalApiParameters().get().getPredicateResource().get(), "UTF-8");

            if (!JsonRelation.validateUri(uri)) {
                return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", "Resource URI not valid"));
            }

            if (entity.filter(requestEntity -> requestEntity.has("_order")).isPresent()) {
                resmiService.moveRelation(resourceUri, RelationMoveOperation.create(entity.get().get("_order").getAsString()));
                return noContent();
            }

            resmiService.createRelation(resourceUri, entity.orElse(null));
            return created();
        } catch (NotFoundException | UnsupportedEncodingException | IllegalArgumentException e) {
            return ErrorResponseFactory.getInstance()
                    .badRequest(new Error("bad_request", e.getClass().getSimpleName() + ": " + e.getMessage()));
        } catch (StartsWithUnderscoreException e) {
            return ErrorResponseFactory.getInstance().invalidEntity("Invalid attribute name \"" + e.getMessage() + "\"");
        }
    }
}
