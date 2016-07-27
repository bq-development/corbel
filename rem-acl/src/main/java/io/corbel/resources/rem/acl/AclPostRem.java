package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

import javax.ws.rs.core.Response;

import com.google.gson.JsonIOException;
import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

/**
 * @author Cristian del Cerro
 */
public class AclPostRem extends AclBaseRem {

    public AclPostRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response collectionWithAcl(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {

        Optional<String> userId = Optional.ofNullable(parameters.getTokenInfo().getUserId());

        if (!userId.isPresent()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (!entity.isPresent()) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        boolean jsonMediaTypeAccepted = parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON);
        InputStream requestBody = entity.get();

        JsonObject jsonObject = new JsonObject();

        if (jsonMediaTypeAccepted) {
            try{
                JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
                jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            } catch (JsonIOException | IllegalStateException ignored) {
                return ErrorResponseFactory.getInstance().badRequest();
            }
            jsonObject.remove(DefaultAclResourcesService._ACL);
        }

        JsonObject acl = new JsonObject();

        JsonObject userAcl = new JsonObject();
        userAcl.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.ADMIN.toString());
        userAcl.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        acl.add(DefaultAclResourcesService.USER_PREFIX + userId.get(), userAcl);

        jsonObject.add(DefaultAclResourcesService._ACL, acl);

        List<Rem> excluded = getExcludedRems(excludedRems);
        Rem postRem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.POST, excluded);
        Response response = aclResourcesService.saveResource(postRem, parameters, type, uri, jsonObject, excluded);

        if (!jsonMediaTypeAccepted) {
            String path = response.getMetadata().getFirst("Location").toString();
            String id = path.substring(path.lastIndexOf("/") + 1);
            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, excluded);

            RequestParameters<ResourceParameters> requestParameters = new RequestParametersImpl<>(null, parameters.getTokenInfo(),
                    parameters.getRequestedDomain(),
                    parameters.getAcceptedMediaTypes(), null, parameters.getHeaders(), null);
            Response responsePutNotJsonResource = aclResourcesService.updateResource(rem, type, new ResourceId(id), requestParameters,
                    requestBody, excluded);
            if (responsePutNotJsonResource.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                return responsePutNotJsonResource;
            }
        }

        return response;

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
        Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.POST, excluded);
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
