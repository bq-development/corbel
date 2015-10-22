package io.corbel.resources.rem.resmi;

import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.AggregationOperator;
import io.corbel.lib.queries.request.Combine;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.BadConfigurationException;
import io.corbel.resources.rem.service.ResmiService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public class ResmiGetRem extends AbstractResmiRem {

    private static final String API_DISTINCT = "api:distinct";
    private static final Logger LOG = LoggerFactory.getLogger(ResmiGetRem.class);

    public ResmiGetRem(ResmiService resmiService) {
        super(resmiService);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildCollectionUri(type);
        try {
            if (parameters.getOptionalApiParameters().flatMap(CollectionParameters::getAggregation).isPresent()) {
                return buildResponse(resmiService.aggregate(resourceUri, parameters.getOptionalApiParameters().get()));
            } else if (parameters.getCustomParameterValue(API_DISTINCT) != null) {
                List<String> fields = getDistinctFields(parameters.getCustomParameterValue(API_DISTINCT));
                return buildResponse(resmiService.findCollectionDistinct(resourceUri, parameters.getOptionalApiParameters(), fields, true));
            } else {
                return buildResponse(resmiService.findCollection(resourceUri, parameters.getOptionalApiParameters()));
            }

        } catch (BadConfigurationException bce) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", bce.getMessage()));
        } catch (Exception e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildResourceUri(type, id.getId());
        return buildResponse(resmiService.findResource(resourceUri));
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<JsonObject> entity) {
        ResourceUri resourceUri = buildRelationUri(type, id.getId(), relation,
                parameters.getOptionalApiParameters().flatMap(RelationParameters::getPredicateResource));
        try {
            if (parameters.getOptionalApiParameters().flatMap(CollectionParameters::getAggregation).isPresent()) {
                return buildResponse(resmiService.aggregate(resourceUri, parameters.getOptionalApiParameters().get()));
            } else if (parameters.getCustomParameterValue(API_DISTINCT) != null) {
                List<String> fields = getDistinctFields(parameters.getCustomParameterValue(API_DISTINCT));
                return buildResponse(resmiService.findRelationDistinct(resourceUri, parameters.getOptionalApiParameters(), fields, true));
            } else {
                return buildResponse(resmiService.findRelation(resourceUri, parameters.getOptionalApiParameters()));
            }
        } catch (Exception e) {
            LOG.error("Failed to get relation data", e);
            // TODO: This should not be a bad request... probably a 500 since most like it is a bug
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }

    private List<String> getDistinctFields(String serializedParameter) {
        return Arrays.asList(serializedParameter.split(",")).stream().map(String::trim).collect(Collectors.toList());
    }
}
