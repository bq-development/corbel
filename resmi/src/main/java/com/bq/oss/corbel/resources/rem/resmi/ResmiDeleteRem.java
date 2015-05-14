package com.bq.oss.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.corbel.resources.rem.service.ResmiService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public class ResmiDeleteRem extends AbstractResmiRem {

    public ResmiDeleteRem(ResmiService resmiService) {
        super(resmiService);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<JsonObject> entity) {
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        resmiService.deleteResourceById(type, id.getId());
        return noContent();
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        Optional<String> dstId = parameters.getApiParameters().getPredicateResource();
        if (!id.isWildcard() || dstId.isPresent()) {
            resmiService.deleteRelation(type, id, relation, dstId);
            return noContent();
        } else {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }
    }

}
