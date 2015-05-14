package com.bq.oss.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.corbel.resources.rem.service.BadConfigurationException;
import com.bq.oss.corbel.resources.rem.service.ResmiService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.model.Error;
import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public class ResmiGetRem extends AbstractResmiRem {

    public ResmiGetRem(ResmiService resmiService) {
        super(resmiService);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<JsonObject> entity) {
        try {
            if (parameters.getApiParameters().getAggregation().isPresent()) {
                return buildResponse(resmiService.aggregate(new ResourceUri(type), parameters.getApiParameters()));
            } else {
                return buildResponse(resmiService.find(type, parameters.getApiParameters()));
            }

        } catch (BadConfigurationException bce) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", bce.getMessage()));
        } catch (Exception e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        return buildResponse(resmiService.findResourceById(type, id));
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        try {
            if (parameters.getApiParameters().getAggregation().isPresent()) {
                return buildResponse(resmiService.aggregate(new ResourceUri(type, id.getId(), relation), parameters.getApiParameters()));
            } else {
                return buildResponse(resmiService.findRelation(type, id, relation, parameters.getApiParameters()));
            }
        } catch (Exception e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

}
