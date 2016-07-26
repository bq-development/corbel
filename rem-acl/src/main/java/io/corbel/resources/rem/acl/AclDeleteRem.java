package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;


/**
 * @author Rubén Carrasco
 */
public class AclDeleteRem extends AclBaseRem {

    public AclDeleteRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response resourceWithAcl(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {
        try {
            if (!aclResourcesService.isAuthorized(parameters.getRequestedDomain(), parameters.getTokenInfo(), type, id, AclPermission.ADMIN)) {
                return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
            }
        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

        List<Rem> excluded = getExcludedRems(excludedRems);
        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.DELETE, excluded);
        return aclResourcesService.deleteResource(rem, type, id, parameters, excluded);
    }

    @Override
    public Response relationWithAcl(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {

        if (id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        try {
            if (!aclResourcesService.isAuthorized(parameters.getRequestedDomain(), parameters.getTokenInfo(), type, id, AclPermission.ADMIN)) {
                return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
            }
        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

        List<Rem> excluded = getExcludedRems(excludedRems);
        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.DELETE, excluded);
        return aclResourcesService.deleteRelation(rem, type, id, relation, parameters, excluded);
    }

}
