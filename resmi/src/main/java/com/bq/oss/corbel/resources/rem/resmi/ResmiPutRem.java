package com.bq.oss.corbel.resources.rem.resmi;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.dao.JsonRelation;
import com.bq.oss.corbel.resources.rem.dao.NotFoundException;
import com.bq.oss.corbel.resources.rem.dao.RelationMoveOperation;
import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.bq.oss.corbel.resources.rem.service.ResmiService;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.model.Error;
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
        return entity.map(object -> {
            try {
                Optional<List<ResourceQuery>> conditions = parameters.getApiParameters().getConditions();
                if (conditions.isPresent()) {
                    JsonObject result = resmiService.conditionalUpdate(type, id.getId(), object, conditions.get());
                    if (result == null) {
                        return ErrorResponseFactory.getInstance().notFound();
                    }
                } else {
                    resmiService.upsert(type, id.getId(), object);
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

        if (id.isWildcard()) {
            ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (parameters.getApiParameters().getPredicateResource().isPresent()) {
            try {
                String uri = URLDecoder.decode(parameters.getApiParameters().getPredicateResource().get(), "UTF-8");
                if (JsonRelation.validateUri(uri)) {

                    JsonObject requestEntity = entity.orElse(null);
                    if (requestEntity != null && requestEntity.has("_order")) {
                        String operation = requestEntity.get("_order").getAsString();
                        resmiService.moveElement(type, id, relation, uri, RelationMoveOperation.create(operation));
                        return noContent();
                    } else {
                        resmiService.createRelation(type, id.getId(), relation, uri, requestEntity);
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
