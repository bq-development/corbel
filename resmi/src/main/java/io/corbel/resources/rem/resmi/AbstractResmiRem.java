package io.corbel.resources.rem.resmi;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.dao.ReservedFields;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.service.ResmiService;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.util.DigestUtils;

import com.google.gson.JsonArray;
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

    protected Response buildResponseWithCustomEtag(JsonElement response) {
        if (response == null) {
            return ErrorResponseFactory.getInstance().notFound();
        } else if (response.isJsonArray()) {
            return buildResponseWithCustomEtag(response, buildEtag(response.getAsJsonArray()));
        } else if (response.isJsonObject()) {
            return buildResponseWithCustomEtag(response, buildEtag(response.getAsJsonObject()));
        } else {
            return buildResponse(response);
        }
    }

    private Response buildResponseWithCustomEtag(JsonElement response, String etag) {
        if (etag == null) {
            buildResponse(response);
        }
        return Response.ok().type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).entity(response).header(HttpHeaders.ETAG, etag).build();
    }

    private String buildEtag(JsonObject response) {
        if (response.has(ReservedFields._UPDATED_AT) && response.has(ReservedFields.ID)) {
            return generateETagHeaderValue(response.get(ReservedFields._UPDATED_AT).getAsString()
                    + response.get(ReservedFields.ID).getAsString());
        } else {
            return null;
        }
    }

    private String buildEtag(JsonArray response) {
        StringBuilder dataToGenerateEtag = new StringBuilder();
        for (JsonElement element : response) {
            final JsonObject elementAsJsonObject = element.getAsJsonObject();
            if (elementAsJsonObject.has(ReservedFields._UPDATED_AT) && elementAsJsonObject.has(ReservedFields.ID)) {
                dataToGenerateEtag.append(elementAsJsonObject.get(ReservedFields._UPDATED_AT).toString());
                dataToGenerateEtag.append(elementAsJsonObject.get(ReservedFields.ID).toString());
            } else {
                return null;
            }
        }
        return generateETagHeaderValue(dataToGenerateEtag.toString());
    }

    private String generateETagHeaderValue(String string) {
        return DigestUtils.md5DigestAsHex(string.getBytes());
    }



    protected ResourceUri buildCollectionUri(String domain, String type) {
        return new ResourceUri(domain, type);
    }

    protected ResourceUri buildResourceUri(String domain, String type, String id) {
        return new ResourceUri(domain, type, id);
    }

    protected ResourceUri buildRelationUri(String domain, String type, String id, String relation, Optional<String> predicateResource) {
        return new ResourceUri(domain, type, id, relation, predicateResource.orElse(null));
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
