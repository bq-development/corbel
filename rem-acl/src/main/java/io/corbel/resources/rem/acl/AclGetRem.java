package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.query.AclQueryBuilder;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.CollectionParametersImpl;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;

/**
 * @author Cristian del Cerro
 */

public class AclGetRem extends AclBaseRem {

    public AclGetRem(AclResourcesService aclResourcesService) {
        super(aclResourcesService, null);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {

        try {
            List<Rem> excluded = getExcludedRems(excludedRems);
            return aclResourcesService.getResourceIfIsAuthorized(parameters.getRequestedDomain(), parameters.getTokenInfo(), type, id, AclPermission.READ).map(originalObject -> {
                if (parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
                    return Response.ok(originalObject).build();
                }

                Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Collections.singletonList(this));
                return aclResourcesService.getResource(rem, type, id, parameters, excluded);

            }).orElseGet(() -> ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.READ)));

        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {
        if (!parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        TokenInfo tokenInfo = parameters.getTokenInfo();

        if (!aclResourcesService.isManagedBy(parameters.getRequestedDomain(), tokenInfo, type)) {
            addAclQueryParams(parameters, tokenInfo);
        }

        List<Rem> excluded = getExcludedRems(excludedRems);
        Rem rem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.GET, excluded);
        Response response = aclResourcesService.getCollection(rem, type, parameters, excluded);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }

        return Response.ok(response.getEntity()).build();
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        try {
            if (!aclResourcesService.isAuthorized(parameters.getRequestedDomain(), parameters.getTokenInfo(), type, id, AclPermission.READ)) {
                return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.READ));
            }
        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

        List<Rem> excluded = getExcludedRems(excludedRems);
        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Collections.singletonList(this));
        return aclResourcesService.getRelation(rem, type, id, relation, parameters, excluded);

    }

    private void addAclQueryParams(RequestParameters<CollectionParameters> parameters, TokenInfo tokenInfo) {

        Optional<CollectionParameters> collectionParameters = parameters.getOptionalApiParameters();

        if (collectionParameters.isPresent()) {
            List<ResourceQuery> aclQueryParams = new AclQueryBuilder(Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups())
                    .build(collectionParameters.flatMap(CollectionParameters::getQueries).orElse(Collections.emptyList()));

            collectionParameters.get().setQueries(Optional.of(aclQueryParams));
        }
    }

}
