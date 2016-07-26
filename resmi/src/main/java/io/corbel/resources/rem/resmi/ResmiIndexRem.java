package io.corbel.resources.rem.resmi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.corbel.lib.mongo.index.MongoIndex;
import io.corbel.lib.mongo.index.MongoTextSearchIndex;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.ResmiService;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Alberto J. Rubio
 *
 */
public class ResmiIndexRem extends BaseRem<JsonObject> {

    private static final String TEXT_SEARCH_INDEX = "text";
    private final ResmiService resmiService;

    public ResmiIndexRem(ResmiService resmiService) {
        this.resmiService = resmiService;
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        ResourceUri resourceUri = new ResourceUri(parameters.getRequestedDomain(), type);
        return entity.map(object -> {
            IndexDefinition indexDefinition = null;
            JsonArray fields = object.getAsJsonArray("fields");
            if(object.has("type") && TEXT_SEARCH_INDEX.equals(object.get("type").getAsString())) {
                MongoTextSearchIndex mongoTextSearchIndex = new MongoTextSearchIndex();
                fields.forEach(field -> mongoTextSearchIndex.on(field.getAsString()));
                indexDefinition = mongoTextSearchIndex.getIndexDefinition();
            } else {
                MongoIndex mongoIndex = new MongoIndex();
                fields.forEach(field -> mongoIndex.on(field.getAsString()));
                indexDefinition = mongoIndex.getIndexDefinition();
            }
            resmiService.ensureIndex(resourceUri, indexDefinition);
            return Response.ok().build();
        }).orElse(ErrorResponseFactory.getInstance().badRequest());
    }

    @Override
    public Class<JsonObject> getType() {
        return JsonObject.class;
    }
}
