package com.bq.oss.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.service.ResmiService;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sánchez - Rubén Carrasco
 */
public abstract class AbstractResmiRem implements Rem<JsonObject> {

    protected final ResmiService resmiService;

    public AbstractResmiRem(ResmiService resmiService) {
        this.resmiService = resmiService;
    }

    protected Response buildResponse(JsonElement response) {
        if (response == null) {
            return ErrorResponseFactory.getInstance().notFound();
        } else {
            return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).build();
        }
    }

    protected Response buildResponse(AggregationResult response) {
        if (response == null) {
            return ErrorResponseFactory.getInstance().notFound();
        } else {
            return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).build();
        }
    }

    protected ResourceUri buildCollectionUri(String type) {
        return new ResourceUri(type);
    }

    protected ResourceUri buildResourceUri(String type, String id) {
        return new ResourceUri(type, id);
    }

    protected ResourceUri buildRelationUri(String type, String id, String relation, RelationParameters apiParameters) {
        return new ResourceUri(type, id, relation, apiParameters.getPredicateResource().orElse(null));
    }

    protected Response noContent() {
        return Response.noContent().build();
    }

    protected Response created() {
        return Response.status(Status.CREATED).build();
    }

    protected Response created(URI location) {
        return Response.created(location).build();
    }

    @Override
    public Class<JsonObject> getType() {
        return JsonObject.class;
    }

}
