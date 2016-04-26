package io.corbel.resources.rem.acl;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.request.builder.RequestParametersBuilder;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.utils.AclUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * @author Cristian del Cerro
 */
public class SetUpAclPutRem extends AclBaseRem {
    private static final String PERMISSION = "permission";
    private static final String PROPERTIES = "properties";
    private final Pattern prefixPattern = Pattern.compile("(?:(?:" + DefaultAclResourcesService.USER_PREFIX + ")|(?:"
            + DefaultAclResourcesService.GROUP_PREFIX + "))\\S+");

    public SetUpAclPutRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        super(aclResourcesService, remsToExclude);
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity,
            Optional<List<Rem>> excludedRems) {

        TokenInfo tokenInfo = parameters.getTokenInfo();

        if (tokenInfo.getUserId() == null) {
            return ErrorResponseFactory.getInstance().methodNotAllowed();
        }

        if (AclUtils.entityIsEmpty(entity)) {
            return ErrorResponseFactory.getInstance().badRequest();
        }

        JsonReader reader = new JsonReader(new InputStreamReader(entity.get()));
        JsonObject jsonObject;

        try {
            jsonObject = new JsonParser().parse(reader).getAsJsonObject();
        } catch (JsonIOException | JsonSyntaxException | IllegalStateException e) {
            return ErrorResponseFactory.getInstance().invalidEntity("Malformed acl object");
        }

        boolean isAuthorized = false;
        List<Rem> excluded = Lists.newArrayList(this);
        excludedRems.ifPresent(excluded::addAll);
        excluded.addAll(remsToExclude);

        try {
            isAuthorized = aclResourcesService.isAuthorized(parameters.getRequestedDomain(), tokenInfo, type, id, AclPermission.ADMIN);
        } catch (AclFieldNotPresentException ignored) {}

        if (!isAuthorized) {
            return ErrorResponseFactory.getInstance().unauthorized(AclUtils.buildMessage(AclPermission.ADMIN));
        }

        JsonObject filteredAclObject = getFilteredAclObject(jsonObject);

        if (!hasAdminPermission(tokenInfo, filteredAclObject)) {
            JsonObject permission = new JsonObject();
            permission.addProperty(PERMISSION,  AclPermission.ADMIN.toString());
            permission.add(PROPERTIES,  new JsonObject());
            filteredAclObject.add(DefaultAclResourcesService.USER_PREFIX + tokenInfo.getUserId(), permission);
        }

        JsonObject objectToSave = new JsonObject();
        objectToSave.add(DefaultAclResourcesService._ACL, filteredAclObject);

        Rem rem = remService.getRem(type, JSON_MEDIATYPE, HttpMethod.PUT, excluded);

        RequestParameters requestParameters = new RequestParametersBuilder<>(parameters).build();
        return aclResourcesService.updateResource(rem, type, id, requestParameters, objectToSave, excluded);

    }

    private JsonObject getFilteredAclObject(JsonObject aclObject) {
        List<String> validFieldNames = aclObject.entrySet().stream().map(Map.Entry::getKey)
                .filter(key -> key.equals(DefaultAclResourcesService.ALL) || prefixPattern.matcher(key).matches())
                .collect(Collectors.toList());

        JsonObject filteredAcl = new JsonObject();

        validFieldNames.forEach(field -> Optional.ofNullable(aclObject.get(field)).filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject).flatMap(this::filterAclValue)
                .ifPresent(filteredAclValue -> filteredAcl.add(field, filteredAclValue)));

        return filteredAcl;
    }

    private Optional<JsonObject> filterAclValue(JsonObject jsonObject) {
        Optional<String> optionalPermission = Optional.ofNullable(jsonObject.get(DefaultAclResourcesService.PERMISSION))
                .filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsJsonPrimitive).filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString);

        try {
            optionalPermission.map(AclPermission::valueOf);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        String permission;

        if (optionalPermission.isPresent()) {
            permission = optionalPermission.get();
        } else {
            return Optional.empty();
        }

        JsonObject properties = Optional.ofNullable(jsonObject.get(DefaultAclResourcesService.PROPERTIES))
                .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject).orElseGet(JsonObject::new);

        JsonObject objectToReturn = new JsonObject();
        objectToReturn.addProperty(DefaultAclResourcesService.PERMISSION, permission);
        objectToReturn.add(DefaultAclResourcesService.PROPERTIES, properties);

        return Optional.of(objectToReturn);
    }

    private boolean idHasAdminPermission(String id, JsonObject acl) {
        return Optional.ofNullable(acl.get(id)).map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get(DefaultAclResourcesService.PERMISSION)).map(JsonElement::getAsString)
                .filter(permissionValue -> permissionValue.equals(AclPermission.ADMIN.toString())).isPresent();
    }

    private boolean hasAdminPermission(TokenInfo tokenInfo, JsonObject acl) {
        return hasAdminPermission(Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), acl);
    }

    private boolean hasAdminPermission(Optional<String> userId, Collection<String> groupIds, JsonObject acl) {
        Stream<String> userAndAllStream = userId
                .map(id -> Arrays.asList(DefaultAclResourcesService.ALL, DefaultAclResourcesService.USER_PREFIX + id))
                .orElseGet(() -> Collections.singletonList(DefaultAclResourcesService.ALL)).stream();
        Stream<String> groupsStream = groupIds.stream().map(id -> DefaultAclResourcesService.GROUP_PREFIX + id);

        return Stream.concat(userAndAllStream, groupsStream).anyMatch(id -> idHasAdminPermission(id, acl));
    }

}
