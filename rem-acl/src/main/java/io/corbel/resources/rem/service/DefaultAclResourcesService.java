package io.corbel.resources.rem.service;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.RemService;

/**
 * @author Cristian del Cerro
 */
public class DefaultAclResourcesService implements AclResourcesService {

    public static final String _ACL = "_acl";
    public static final String ALL = "ALL";
    public static final String USER = "user";
    public static final String GROUP = "group";
    public static final char SEPARATOR = ':';
    public static final String USER_PREFIX = USER + SEPARATOR;
    public static final String GROUP_PREFIX = GROUP + SEPARATOR;
    public static final String ALL_COLLECTIONS = "*";
    public static final String PERMISSION = "permission";
    public static final String PROPERTIES = "properties";

    private RemService remService;
    private Rem resmiRem;

    @Override
    @SuppressWarnings("unchecked")
    public Response saveResource(Rem rem, RequestParameters<CollectionParameters> parameters, String type, URI uri, Object entity) {
        return rem.collection(type, parameters, uri, Optional.of(entity));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters) {
        return rem.resource(type, id, parameters, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getCollection(Rem rem, String type, RequestParameters<CollectionParameters> parameters) {
        return rem.collection(type, parameters, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters) {
        return rem.relation(type, id, relation, parameters, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response updateResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Object entity) {
        return rem.resource(type, id, parameters, Optional.of(entity));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response putRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Object entity) {
        return rem.relation(type, id, relation, parameters, Optional.of(entity));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response deleteResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters) {
        return rem.resource(type, id, parameters, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response deleteRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters) {
        return rem.relation(type, id, relation, parameters, null);
    }

    @Override
    public boolean isAuthorized(String userId, Collection<String> groupIds, String type, ResourceId resourceId, AclPermission operation) {
        return getResourceIfIsAuthorized(userId, groupIds, type, resourceId, operation).isPresent();
    }

    @Override
    public Optional<JsonObject> getResourceIfIsAuthorized(String userId, Collection<String> groupIds, String type, ResourceId resourceId,
            AclPermission operation) {
        initResmiRem();

        @SuppressWarnings("unchecked")
        Response response = resmiRem.resource(type, resourceId, null, null);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException(response);
        }

        JsonObject originalObject;

        try {
            originalObject = (JsonObject) response.getEntity();
        } catch (ClassCastException e) {
            return Optional.empty();
        }

        return Optional.ofNullable(originalObject.get(_ACL)).filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject).filter(acl -> checkAclEntry(acl, ALL, operation)
                        || checkAclEntry(acl, USER_PREFIX + userId, operation) || checkAclEntry(acl, GROUP_PREFIX, groupIds, operation))
                .map(acl -> originalObject);
    }

    private void initResmiRem() {
        if (resmiRem == null) {
            resmiRem = remService.getRem(ALL_COLLECTIONS, Collections.singletonList(MediaType.APPLICATION_JSON), HttpMethod.GET);
        }
    }

    private boolean checkAclEntry(JsonObject acl, String prefix, Collection<String> ids, AclPermission operation) {
        return ids.stream().map(id -> prefix + id).anyMatch(id -> checkAclEntry(acl, id, operation));
    }

    private boolean checkAclEntry(JsonObject acl, String id, AclPermission operation) {
        return Optional.ofNullable(acl.get(id)).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get(PERMISSION)).flatMap(jsonElement -> {
                    try {
                        return Optional.ofNullable(jsonElement.getAsString());
                    } catch (ClassCastException | IllegalStateException | UnsupportedOperationException e) {
                        return Optional.empty();
                    }
                }).filter(permissionString -> AclPermission.valueOf(permissionString).canPerform(operation)).isPresent();
    }

    @Override
    public void setRemService(RemService remService) {
        this.remService = remService;
    }

}
