package io.corbel.resources.rem.service;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.model.RemDescription;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.request.builder.RequestParametersBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * @author Cristian del Cerro
 */
public class DefaultAclResourcesService implements AclResourcesService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAclResourcesService.class);

    public static final String _ACL = "_acl";
    public static final String ALL = "ALL";
    public static final String USER = "user";
    public static final String GROUP = "group";
    public static final char SEPARATOR = ':';
    public static final String USER_PREFIX = USER + SEPARATOR;
    public static final String GROUP_PREFIX = GROUP + SEPARATOR;
    public static final String PERMISSION = "permission";
    public static final String PROPERTIES = "properties";
    public static final char JOIN_CHAR = ':';
    public static final String RESMI_GET = "ResmiGetRem";
    public static final String RESMI_PUT = "ResmiPutRem";

    private final Pattern collectionPattern = Pattern.compile("^(?:.*/)?[\\w-_]+?(?::(?<collection>[\\w-_:]+))?$");

    private RemService remService;
    private Rem resmiGetRem;
    private Rem resmiPutRem;
    private final Gson gson;
    private final String adminsCollection;
    private List<Pair<Rem, HttpMethod>> remsAndMethods = Collections.emptyList();

    public DefaultAclResourcesService(Gson gson, String adminsCollection) {
        this.gson = gson;
        this.adminsCollection = adminsCollection;
    }

    @Override
    public void setRemsAndMethods(List<Pair<Rem, HttpMethod>> remsAndMethods) {
        this.remsAndMethods = remsAndMethods;
    }

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
    public boolean isAuthorized(TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException {
        return isAuthorized(tokenInfo.getDomainId(), Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), type, resourceId,
                operation);
    }

    @Override
    public boolean isAuthorized(String domainId, Optional<String> userId, Collection<String> groupIds, String type, ResourceId resourceId,
            AclPermission operation) throws AclFieldNotPresentException {
        return getResourceIfIsAuthorized(domainId, userId, groupIds, type, resourceId, operation).isPresent();
    }

    @Override
    public boolean isManagedBy(TokenInfo tokenInfo, String collection) {
        return isManagedBy(tokenInfo.getDomainId(), Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), collection);
    }

    @Override
    public boolean isManagedBy(String domainId, Optional<String> userId, Collection<String> groupIds, String collection) {
        if (!userId.isPresent() && groupIds.isEmpty()) {
            return false;
        }

        Optional<ManagedCollection> collectionManagers = getManagers(domainId, collection);

        if (collectionManagers.filter(presentCollectionManagers -> verifyPresence(userId, groupIds, presentCollectionManagers)).isPresent()) {
            return true;
        }

        Optional<ManagedCollection> domainManagers = getManagers(domainId);

        return domainManagers.filter(presentDomainManagers -> verifyPresence(userId, groupIds, presentDomainManagers)).isPresent();
    }

    private Optional<ManagedCollection> getManagers(String domainId, String collection) {
        return getManagers(domainId + JOIN_CHAR + collection);
    }

    private Optional<ManagedCollection> getManagers(String collection) {
        Optional<JsonObject> response;

        try {
            response = getResource(adminsCollection, new ResourceId(collection));
        }

        catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return Optional.empty();
            }

            throw e;
        }

        return response.flatMap(this::objectToManagedCollection);
    }

    private Optional<ManagedCollection> objectToManagedCollection(Object object) {
        try {
            return Optional.of(gson.fromJson((JsonElement) object, ManagedCollection.class));
        } catch (ClassCastException | JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    private boolean verifyPresence(Optional<String> userId, Collection<String> groupIds, ManagedCollection managedCollection) {
        return userId.map(id -> managedCollection.getUsers().contains(id)).orElse(false)
                || managedCollection.getGroups().stream().anyMatch(groupIds::contains);
    }

    private Optional<JsonObject> getResource(String type, ResourceId resourceId) {

        @SuppressWarnings("unchecked")
        Response response = getResmiGetRem().resource(type, resourceId, null, Optional.empty());

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new WebApplicationException(response);
        }

        JsonObject originalObject;

        try {
            originalObject = (JsonObject) response.getEntity();
        } catch (ClassCastException e) {
            return Optional.empty();
        }

        return Optional.of(originalObject);

    }

    @Override
    public Optional<JsonObject> getResourceIfIsAuthorized(TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException {
        return getResourceIfIsAuthorized(tokenInfo.getDomainId(), Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), type,
                resourceId, operation);
    }

    @Override
    public Optional<JsonObject> getResourceIfIsAuthorized(String domainId, Optional<String> userId, Collection<String> groupIds,
            String type, ResourceId resourceId, AclPermission operation) throws AclFieldNotPresentException {

        if (isManagedBy(domainId, userId, groupIds, type)) {
            return getResource(type, resourceId);
        }

        Optional<JsonObject> originalObject = getResource(type, resourceId);

        Optional<JsonElement> aclObject = originalObject.map(resource -> resource.get(_ACL));

        if (!aclObject.isPresent()) {
            throw new AclFieldNotPresentException();
        }

        return aclObject
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .filter(acl -> checkAclEntry(acl, ALL, operation)
                        || userId.filter(id -> checkAclEntry(acl, USER_PREFIX + id, operation)).isPresent()
                        || checkAclEntry(acl, GROUP_PREFIX, groupIds, operation)).flatMap(acl -> originalObject);

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
    public Response updateConfiguration(ResourceId id, RequestParameters<ResourceParameters> parameters, ManagedCollection managedCollection) {
        JsonObject jsonObject = gson.toJsonTree(managedCollection).getAsJsonObject();
        return updateResource(getResmiPutRem(), adminsCollection, id, parameters, jsonObject);
    }

    @Override
    public void addAclConfiguration(String collection) {
        List<RemDescription> remDescriptions = remService.getRegisteredRemDescriptions();

        boolean alreadyRegistered = remDescriptions.stream().anyMatch(
                description -> description.getUriPattern().equals(collection) && description.getRemName().startsWith("Acl"));

        if (alreadyRegistered) {
            return;
        }

        String collectionPattern = collection + "(?:/.*)?";
        remsAndMethods.forEach(remAndMethod -> remService.registerRem(remAndMethod.getLeft(), collectionPattern, remAndMethod.getRight()));
    }

    @Override
    public void removeAclConfiguration(String collection) {
        String collectionPattern = collection + "(?:/.*)?";
        remsAndMethods.stream().map(Pair::getLeft).forEach(aclRem -> remService.unregisterRem(aclRem.getClass(), collectionPattern));
    }

    @Override
    public void refreshRegistry() {
        RequestParameters requestParameters = new RequestParametersBuilder().build();
        Response response = getCollection(getResmiGetRem(), adminsCollection, requestParameters);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            LOG.error("Can't access {}", adminsCollection);
            return;
        }

        JsonArray jsonArray;

        try {
            jsonArray = (JsonArray) response.getEntity();
        } catch (ClassCastException e) {
            LOG.error("Can't read " + adminsCollection + " properly", e);
            return;
        }

        for (JsonElement jsonElement : jsonArray) {

            Optional<JsonObject> idField = Optional.of(jsonElement).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                    .filter(jsonObject -> jsonObject.has("id"));

            if (!idField.isPresent()) {
                LOG.error("Document in acl configuration collection has no id field: {}", jsonElement.toString());
                continue;
            }

            Optional<String> id = idField.map(jsonObject -> jsonObject.get("id")).filter(JsonElement::isJsonPrimitive)
                    .map(JsonElement::getAsJsonPrimitive).filter(JsonPrimitive::isString).map(JsonPrimitive::getAsString);

            if (!id.isPresent()) {
                LOG.error("Unrecognized id: {}", jsonElement.toString());
                continue;
            }

            id.flatMap(string -> {
                Matcher matcher = collectionPattern.matcher(string);

                if (matcher.matches()) {
                    return Optional.of(matcher.group("collection")).filter(collection -> !collection.isEmpty());

                } else {
                    return Optional.empty();
                }
            }).ifPresent(this::addAclConfiguration);

        }

    }

    private Rem getResmiGetRem() {
        if (resmiGetRem == null) {
            resmiGetRem = remService.getRem(RESMI_GET);
        }
        return resmiGetRem;
    }

    private Rem getResmiPutRem() {
        if (resmiPutRem == null) {
            resmiPutRem = remService.getRem(RESMI_PUT);
        }
        return resmiPutRem;
    }

    @Override
    public void setRemService(RemService remService) {
        this.remService = remService;
    }

}
