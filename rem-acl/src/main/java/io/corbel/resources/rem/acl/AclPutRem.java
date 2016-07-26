package io.corbel.resources.rem.acl;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Rubén Carrasco
 */
public class AclPutRem extends AclBaseRem {

    public AclPutRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response resourceWithAcl(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {

        TokenInfo tokenInfo = parameters.getTokenInfo();
        Optional<String> userId = Optional.ofNullable(tokenInfo.getUserId());

        if (!userId.isPresent()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (!entity.isPresent()) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        boolean newResource = false;
        Optional<JsonObject> originalObject = Optional.empty();

        try {
            originalObject = aclResourcesService.getResourceIfIsAuthorized(parameters.getRequestedDomain(), tokenInfo, type, id, AclPermission.WRITE);
        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() != Status.NOT_FOUND.getStatusCode()) {
                return exception.getResponse();
            }
            newResource = true;
        }

        List<Rem> excluded = getExcludedRems(excludedRems);

        if (newResource) {

            JsonObject aclValue = new JsonObject();
            aclValue.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.ADMIN.toString());
            aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

            JsonObject user = new JsonObject();
            userId.ifPresent(presentUserId -> user.add(DefaultAclResourcesService.USER_PREFIX + presentUserId, aclValue));

            JsonObject acl = new JsonObject();
            acl.add(DefaultAclResourcesService._ACL, user);

            Rem rem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.PUT, Collections.singletonList(this));
            Response responseSetAcl = aclResourcesService.updateResource(rem, type, id, parameters, acl, excluded);

            if (responseSetAcl.getStatus() != Status.NO_CONTENT.getStatusCode()) {
                return responseSetAcl;
            }

        } else if (!originalObject.isPresent()) {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.WRITE));
        }

        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, excluded);

        InputStream requestBody = entity.get();

        if (parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON)) {
            JsonObject jsonObject;
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
                jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            } catch (IllegalStateException ignored) {
                return ErrorResponseFactory.getInstance().badRequest();
            }
            jsonObject.remove(DefaultAclResourcesService._ACL);
            return aclResourcesService.updateResource(rem, type, id, parameters, jsonObject, excluded);
        }

        return aclResourcesService.updateResource(rem, type, id, parameters, requestBody, excluded);
    }

    @Override
    public Response relationWithAcl(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
                             Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {

        TokenInfo tokenInfo = parameters.getTokenInfo();

        if (tokenInfo.getUserId() == null || id.isWildcard()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (!entity.isPresent()) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        try {
            if (!aclResourcesService.isAuthorized(parameters.getRequestedDomain(), tokenInfo, type, id, AclPermission.WRITE)) {
                return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.WRITE));
            }
        } catch (AclFieldNotPresentException e) {
            return ErrorResponseFactory.getInstance().forbidden();
        }

        List<Rem> excluded = getExcludedRems(excludedRems);
        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, excluded);
        JsonObject jsonObject;

        try {
            JsonReader reader = new JsonReader(new InputStreamReader(entity.get()));
            jsonObject = new JsonParser().parse(reader).getAsJsonObject();
        } catch (JsonIOException | IllegalStateException ignored) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        return aclResourcesService.putRelation(rem, type, id, relation, parameters, jsonObject, excluded);
    }

}
