package io.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.service.ResmiService;
import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
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

    protected ResourceUri buildRelationUri(String type, String id, String relation,Optional<String> predicateResource) {
        return new ResourceUri(type, id, relation, predicateResource.orElse(null));
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
