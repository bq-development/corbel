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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;


import com.google.gson.*;

/**
 * @author Cristian del Cerro
 */
public class DefaultAclResourcesService implements AclResourcesService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAclResourcesService.class);

    private static final String REGISTRY_DOMAIN = "_silkroad";


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
    public Response saveResource(Rem rem, RequestParameters<CollectionParameters> parameters, String type, URI uri, Object entity, List<Rem> excludedRems) {
        return rem.collection(type, parameters, uri, Optional.of(entity), Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, List<Rem> excludedRems) {
        return rem.resource(type, id, parameters, null, Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getCollection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, List<Rem> excludedRems) {
        return rem.collection(type, parameters, null, null, Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, List<Rem> excludedRems) {
        return rem.relation(type, id, relation, parameters, null, Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response updateResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Object entity, List<Rem> excludedRems) {
        return rem.resource(type, id, parameters, Optional.of(entity), Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response putRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Object entity, List<Rem> excludedRems) {
        return rem.relation(type, id, relation, parameters, Optional.of(entity), Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response deleteResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, List<Rem> excludedRems) {
        return rem.resource(type, id, parameters, null, Optional.ofNullable(excludedRems));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response deleteRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, List<Rem> excludedRems) {
        return rem.relation(type, id, relation, parameters, null, Optional.ofNullable(excludedRems));
    }

    @Override
    public boolean isAuthorized(String requestedDomain, TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException {
        return isAuthorized(requestedDomain, tokenInfo.getDomainId(), Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), type, resourceId,
                operation);
    }

    private boolean isAuthorized(String requestedDomain, String domainId, Optional<String> userId, Collection<String> groupIds, String type, ResourceId resourceId,
            AclPermission operation) throws AclFieldNotPresentException {
        return getResourceIfIsAuthorized(requestedDomain, domainId, userId, groupIds, type, resourceId, operation).isPresent();
    }

    @Override
    public boolean isManagedBy(String requestedDomain, TokenInfo tokenInfo, String collection) {
        return isManagedBy(requestedDomain, Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), collection);
    }

    private boolean isManagedBy(String requestedDomain, Optional<String> userId, Collection<String> groupIds, String collection) {
        if (!userId.isPresent() && groupIds.isEmpty()) {
            return false;
        }

        Optional<ManagedCollection> collectionManagers = getManagers(requestedDomain, collection);

        if (collectionManagers.filter(presentCollectionManagers -> verifyPresence(userId, groupIds, presentCollectionManagers)).isPresent()) {
            return true;
        }

        Optional<ManagedCollection> domainManagers = getManagers(requestedDomain);

        return domainManagers.filter(presentDomainManagers -> verifyPresence(userId, groupIds, presentDomainManagers)).isPresent();
    }

    private Optional<ManagedCollection> getManagers(String domainId, String collection) {
        return getManagers(domainId + JOIN_CHAR + collection);
    }

    private Optional<ManagedCollection> getManagers(String collection) {
        Optional<JsonObject> response;

        try {
            response = getResource(REGISTRY_DOMAIN, adminsCollection, new ResourceId(collection));
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

    private Optional<JsonObject> getResource(String domain, String type, ResourceId resourceId) {
        RequestParameters requestParameters = new RequestParametersBuilder().setRequestedDomain(domain).build();
        @SuppressWarnings("unchecked")
        Response response = getResmiGetRem().resource(type, resourceId, requestParameters, Optional.empty());

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
    public Optional<JsonObject> getResourceIfIsAuthorized(String requestedDomain, TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException {
        return getResourceIfIsAuthorized(requestedDomain, tokenInfo.getDomainId(), Optional.ofNullable(tokenInfo.getUserId()), tokenInfo.getGroups(), type,
                resourceId, operation);
    }

    private Optional<JsonObject> getResourceIfIsAuthorized(String requestedDomain, String domainId, Optional<String> userId, Collection<String> groupIds,
            String type, ResourceId resourceId, AclPermission operation) throws AclFieldNotPresentException {

        Optional<JsonObject> originalObject = getResource(requestedDomain, type, resourceId);

        if (isManagedBy(domainId, userId, groupIds, type)) {
            return originalObject;
        }

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
    public Response updateConfiguration(ResourceId id, ManagedCollection managedCollection) {
        JsonObject jsonObject = gson.toJsonTree(managedCollection).getAsJsonObject();
        RequestParameters requestParameters = new RequestParametersBuilder().setRequestedDomain(REGISTRY_DOMAIN).build();
        return updateResource(getResmiPutRem(), adminsCollection, id, requestParameters, jsonObject, Collections.emptyList());
    }

    @Override
    public void addAclConfiguration(String pattern) {
        List<RemDescription> remDescriptions = remService.getRegisteredRemDescriptions();

        boolean alreadyRegistered = remDescriptions.stream().anyMatch(
                description -> description.getUriPattern().equals(pattern) && description.getRemName().startsWith("Acl"));

        if (alreadyRegistered) {
            return;
        }

        remsAndMethods.forEach(remAndMethod -> remService.registerRem(remAndMethod.getLeft(), pattern, remAndMethod.getRight()));
    }

    @Override
    public void removeAclConfiguration(String pattern) {
        remsAndMethods.stream().map(Pair::getLeft).forEach(aclRem -> remService.unregisterRem(aclRem.getClass(), pattern));
    }

    @Override
    public void refreshRegistry() {
        RequestParameters requestParameters =  new RequestParametersBuilder().setRequestedDomain(REGISTRY_DOMAIN).build();
        Response response = getCollection(getResmiGetRem(), adminsCollection, requestParameters, Collections.emptyList());

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

            Optional<String> idOptional = idField.map(jsonObject -> jsonObject.get("id")).filter(JsonElement::isJsonPrimitive)
                    .map(JsonElement::getAsJsonPrimitive).filter(JsonPrimitive::isString).map(JsonPrimitive::getAsString);

            if (!idOptional.isPresent()) {
                LOG.error("Unrecognized id: {}", jsonElement.toString());
                continue;
            }

            String id = idOptional.get();
            // TODO: Ignoring domain by the moment (rem registration by domain needed)
            addAclConfiguration(id.substring(id.indexOf(":") + 1));

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
