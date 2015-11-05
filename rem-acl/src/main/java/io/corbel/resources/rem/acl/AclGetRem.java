package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.google.gson.JsonObject;

import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.acl.query.AclQueryBuilder;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

/**
 * @author Cristian del Cerro
 */

public class AclGetRem extends AclBaseRem {

    public AclGetRem(AclResourcesService aclResourcesService) {
        super(aclResourcesService, null);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {

        try {

            return aclResourcesService.getResourceIfIsAuthorized(parameters.getTokenInfo(), type, id, AclPermission.READ).map(originalObject -> {
                if (parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
                    return Response.ok(originalObject).build();
                }

                Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Collections.singletonList(this));
                return aclResourcesService.getResource(rem, type, id, parameters);

            }).orElseGet(() -> ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.READ)));

        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {
        if (!parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        TokenInfo tokenInfo = parameters.getTokenInfo();

        if (!aclResourcesService.isManagedBy(tokenInfo, type)) {
            addAclQueryParams(parameters, tokenInfo);
        }

        Rem resmiRem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.GET, Collections.singletonList(this));
        Response response = aclResourcesService.getCollection(resmiRem, type, parameters);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }

        return Response.ok(response.getEntity()).build();
    }

    private void addAclQueryParams(RequestParameters<CollectionParameters> parameters, TokenInfo tokenInfo) {

        Optional<CollectionParameters> collectionParameters = parameters.getOptionalApiParameters();

        List<ResourceQuery> aclQueryParams = new AclQueryBuilder(Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups())
                .build(collectionParameters.flatMap(CollectionParameters::getQueries).orElse(Collections.emptyList()));

        if (collectionParameters.isPresent()) {
            collectionParameters.get().setQueries(Optional.of(aclQueryParams));
        } else {
            CollectionParametersImpl collectionParametersImpl = new CollectionParametersImpl(null, Optional.empty(),
                    Optional.of(aclQueryParams), Optional.empty(), Optional.empty(), Optional.empty());
            collectionParameters = Optional.of(collectionParametersImpl);
        }

    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity) {

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        try {
            if (!aclResourcesService.isAuthorized(parameters.getTokenInfo(), type, id, AclPermission.READ)) {
                return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.READ));
            }
        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Collections.singletonList(this));
        return aclResourcesService.getRelation(rem, type, id, relation, parameters);

    }

}
