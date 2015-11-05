package io.corbel.resources.rem.acl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.text.html.Option;
import javax.ws.rs.core.Response;

import io.corbel.lib.token.TokenInfo;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
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
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {

        Optional<String> userId = Optional.ofNullable(parameters.getTokenInfo().getUserId());

        if (!userId.isPresent()) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (AclUtils.entityIsEmpty(entity)) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        boolean jsonMediaTypeAccepted = parameters.getAcceptedMediaTypes().contains(MediaType.APPLICATION_JSON);
        InputStream requestBody = entity.get();

        JsonObject jsonObject = new JsonObject();

        if (jsonMediaTypeAccepted) {
            JsonReader reader = new JsonReader(new InputStreamReader(requestBody));
            jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            jsonObject.remove(DefaultAclResourcesService._ACL);
        }

        JsonObject acl = new JsonObject();

        JsonObject userAcl = new JsonObject();
        userAcl.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.ADMIN.toString());
        userAcl.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        acl.add(DefaultAclResourcesService.USER_PREFIX + userId.get(), userAcl);

        jsonObject.add(DefaultAclResourcesService._ACL, acl);

        Rem resmiPostRem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.POST, Collections.singletonList(this));
        Response response = aclResourcesService.saveResource(resmiPostRem, parameters, type, uri, jsonObject);

        if (!jsonMediaTypeAccepted) {
            String path = response.getMetadata().getFirst("Location").toString();
            String id = path.substring(path.lastIndexOf("/") + 1);
            Rem rem = remService.getRem(type, parameters.getAcceptedMediaTypes(), HttpMethod.PUT, REMS_TO_EXCLUDE);

            RequestParameters<ResourceParameters> requestParameters = new RequestParametersImpl<>(null, null,
                    parameters.getAcceptedMediaTypes(), null, parameters.getHeaders(), null);
            Response responsePutNotJsonResource = aclResourcesService.updateResource(rem, type, new ResourceId(id), requestParameters,
                    requestBody);
            if (responsePutNotJsonResource.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                return responsePutNotJsonResource;
            }
        }

        return response;

    }

}
