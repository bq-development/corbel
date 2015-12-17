package io.corbel.resources.rem.resmi;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.ResmiService;

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
        ResourceUri uriResource = buildCollectionUri(parameters.getRequestedDomain(), type);
        resmiService.deleteCollection(uriResource, parameters.getOptionalApiParameters().flatMap(CollectionParameters::getQueries));
        return noContent();
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildResourceUri(parameters.getRequestedDomain(), type, id.getId());
        resmiService.deleteResource(resourceUri);
        return noContent();
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        Optional<RelationParameters> apiParameters = parameters.getOptionalApiParameters();
        ResourceUri uri = buildRelationUri(parameters.getRequestedDomain(), type, id.getId(), relation, apiParameters.flatMap(RelationParameters::getPredicateResource));
        if (!id.isWildcard() || uri.getRelationId() != null) {
            resmiService.deleteRelation(uri, apiParameters.flatMap(RelationParameters::getQueries));
            return noContent();
        } else {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }
    }

}
