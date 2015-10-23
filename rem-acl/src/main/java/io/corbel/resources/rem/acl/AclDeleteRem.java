package io.corbel.resources.rem.acl;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;


/**
 * @author Rubén Carrasco
 */
public class AclDeleteRem extends AclBaseRem {

    public AclDeleteRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {

        String userId = parameters.getTokenInfo().getUserId();
        if (userId == null) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        Collection<String> groupIds = parameters.getTokenInfo().getGroups();

        if (aclResourcesService.isAuthorized(userId, groupIds, type, id, AclPermission.ADMIN)) {
            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.DELETE, Collections.singletonList(this));
            return aclResourcesService.deleteResource(rem, type, id, parameters);
        }

        return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Optional<InputStream> entity) {

        String userId = parameters.getTokenInfo().getUserId();
        Collection<String> groupIds = parameters.getTokenInfo().getGroups();

        if (!id.isWildcard() && userId != null) {
            if (aclResourcesService.isAuthorized(userId, groupIds, type, id, AclPermission.ADMIN)) {
                Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.DELETE, Collections.singletonList(this));
                return aclResourcesService.deleteRelation(rem, type, id, relation, parameters);
            }
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
        } else {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }
    }

}
