package io.corbel.resources.rem.acl;

import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.query.AclQueryBuilder;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.CollectionParametersImpl;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * @author Cristian del Cerro
 */

public class AclGetRem extends AclBaseRem {

    public AclGetRem(AclResourcesService aclResourcesService) {
        super(aclResourcesService, null);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {

        String userId = parameters.getTokenInfo().getUserId();
        Collection<String> groupIds = parameters.getTokenInfo().getGroups();

        return aclResourcesService.getResourceIfIsAuthorized(userId, groupIds, type, id, AclPermission.READ).map(originalObject -> {
            if (parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
                return Response.ok(originalObject).build();
            }

            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Collections.singletonList(this));
            return aclResourcesService.getResource(rem, type, id, parameters);

        }).orElseGet(() -> ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.READ)));
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {
        String userId = parameters.getTokenInfo().getUserId();
        Collection<String> groupIds = parameters.getTokenInfo().getGroups();
        if (!parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        addAclQueryParams(parameters, userId, groupIds);

        Rem resmiRem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.GET, Collections.singletonList(this));
        Response response = aclResourcesService.getCollection(resmiRem, type, parameters);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }

        return Response.ok(response.getEntity()).build();
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity) {

        String userId = parameters.getTokenInfo().getUserId();
        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        Collection<String> groupIds = parameters.getTokenInfo().getGroups();

        if (aclResourcesService.isAuthorized(userId, groupIds, type, id, AclPermission.READ)) {
            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Collections.singletonList(this));
            return aclResourcesService.getRelation(rem, type, id, relation, parameters);
        }
        return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.READ));

    }

    private void addAclQueryParams(RequestParameters<CollectionParameters> parameters, String userId, Collection<String> groupIds) {
        Optional<CollectionParameters> collectionParameters = parameters.getOptionalApiParameters();

        List<ResourceQuery> aclQueryParams = new AclQueryBuilder(userId, groupIds).build(collectionParameters.flatMap(
                CollectionParameters::getQueries).orElse(Collections.emptyList()));

        if (collectionParameters.isPresent()) {
            collectionParameters.get().setQueries(Optional.of(aclQueryParams));
        } else {
            CollectionParametersImpl collectionParametersImpl = new CollectionParametersImpl(null, Optional.empty(),
                    Optional.of(aclQueryParams), Optional.empty(), Optional.empty(), Optional.empty());
            collectionParameters = Optional.of(collectionParametersImpl);
        }
    }

}
