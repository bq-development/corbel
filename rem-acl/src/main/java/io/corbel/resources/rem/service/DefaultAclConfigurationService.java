package io.corbel.resources.rem.service;

import io.corbel.lib.queries.builder.QueryParametersBuilder;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.model.RemDescription;
import io.corbel.resources.rem.request.CollectionParametersImpl;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.request.builder.RequestParametersBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DefaultAclConfigurationService implements AclConfigurationService {

    public static final String COLLECTION_NAME_FIELD = "collectionName";
    public static final String DOMAIN_FIELD = "domain";
    public static final String DEFAULT_PERMISSION_FIELD = "defaultPermission";
    private static final String ALL_URIS_REGEXP = "(/.*)?";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAclConfigurationService.class);
    public static final char JOIN_CHAR = ':';
    public static final String REGISTRY_DOMAIN = "_silkroad";
    public static final String RESMI_GET = "ResmiGetRem";
    public static final String RESMI_PUT = "ResmiPutRem";
    public static final String RESMI_POST = "ResmiPostRem";
    private static final String RESMI_DELETE = "ResmiDeleteRem";

    private final Gson gson;
    private final String adminsCollection;
    private List<Pair<Rem, HttpMethod>> remsAndMethods = Collections.emptyList();
    private RemService remService;
    private Rem resmiGetRem;
    private Rem resmiPutRem;
    private Rem resmiPostRem;
    private Rem resmiDeleteRem;
    private final AclResourcesService aclResourcesService;

    public DefaultAclConfigurationService(Gson gson, String adminsCollection, AclResourcesService aclResourcesService) {
        this.gson = gson;
        this.adminsCollection = adminsCollection;
        this.aclResourcesService = aclResourcesService;
    }

    @Override
    public void setRemsAndMethods(List<Pair<Rem, HttpMethod>> remsAndMethods) {
        this.remsAndMethods = remsAndMethods;
    }

    @Override
    public Response getConfigurations(String domain) {
        return aclResourcesService.getCollection(
                getResmiGetRem(),
                adminsCollection,
                new RequestParametersBuilder(REGISTRY_DOMAIN).apiParameters(
                        new CollectionParametersImpl(new QueryParametersBuilder().queries(
                                new ResourceQueryBuilder().add(DOMAIN_FIELD, domain).build()).build())).build(), Collections.emptyList());
    }

    @Override
    public Response getConfiguration(String id, String domain) {
        return getResourceWithParameters(
                id,
                new RequestParametersBuilder(REGISTRY_DOMAIN).apiParameters(
                        new CollectionParametersImpl(new QueryParametersBuilder().condition(
                                new ResourceQueryBuilder().add(DOMAIN_FIELD, domain).build()).build())).build());
    }

    @Override
    public Response getConfiguration(String id) {
        return getResourceWithParameters(id, new RequestParametersBuilder(REGISTRY_DOMAIN).build());
    }

    private Response getResourceWithParameters(String id, RequestParameters<ResourceParameters> parameters) {
        return aclResourcesService.getResource(getResmiGetRem(), adminsCollection, new ResourceId(id), parameters, Collections.emptyList());
    }

    @Override
    public Response createConfiguration(URI uri, ManagedCollection managedCollection) {
        JsonObject jsonObject = gson.toJsonTree(managedCollection).getAsJsonObject();
        RequestParameters requestParameters = new RequestParametersBuilder(REGISTRY_DOMAIN).build();
        return aclResourcesService.saveResource(getResmiPostRem(), requestParameters, adminsCollection, uri, jsonObject,
                Collections.emptyList());
    }

    @Override
    public Response updateConfiguration(String id, ManagedCollection managedCollection) {
        RequestParameters requestParameters = new RequestParametersBuilder(REGISTRY_DOMAIN).build();
        ResourceId resourceId = new ResourceId(id);
        Response response = aclResourcesService.getResource(getResmiGetRem(), adminsCollection, resourceId, requestParameters,
                Collections.emptyList());
        if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            return ErrorResponseFactory.getInstance().preconditionFailed("Can't create a acl configuration with PUT method.");
        }
        JsonObject jsonObject = gson.toJsonTree(managedCollection).getAsJsonObject();
        return aclResourcesService.updateResource(getResmiPutRem(), adminsCollection, resourceId, requestParameters, jsonObject,
                Collections.emptyList());
    }

    @Override
    public void addAclConfiguration(String collectionName) {
        List<RemDescription> remDescriptions = remService.getRegisteredRemDescriptions();

        boolean alreadyRegistered = remDescriptions.stream().anyMatch(
                description -> description.getUriPattern().equals(collectionName) && description.getRemName().startsWith("Acl"));

        if (alreadyRegistered) {
            return;
        }

        remsAndMethods.forEach(remAndMethod -> remService.registerRem(remAndMethod.getLeft(), getRemPattern(collectionName),
                remAndMethod.getRight()));
    }

    @Override
    public void removeAclConfiguration(String id, String collectionName) {
        remsAndMethods.stream().map(Pair::getLeft)
                .forEach(aclRem -> remService.unregisterRem(aclRem.getClass(), getRemPattern(collectionName)));

        RequestParameters parameters = new RequestParametersBuilder(REGISTRY_DOMAIN).build();
        aclResourcesService.deleteResource(getResmiDeleteRem(), adminsCollection, new ResourceId(id), parameters, Collections.emptyList());

    }

    private String getRemPattern(String collectionName) {
        return collectionName + ALL_URIS_REGEXP;
    }

    @Override
    public void setResourcesWithDefaultPermission(String collectionName, String domain, String defaultPermission) {
        JsonObject aclObject = constructAclObjectWithDefaultPermission(defaultPermission);
        RequestParameters parameters = new RequestParametersBuilder(domain).build();
        getResmiPutRem().collection(collectionName, parameters, null, Optional.of(aclObject));
    }

    private JsonObject constructAclObjectWithDefaultPermission(String defaultPermission) {
        JsonObject aclObject = new JsonObject();
        JsonObject allObject = new JsonObject();
        JsonObject allContentObject = new JsonObject();
        allContentObject.addProperty(DefaultAclResourcesService.PERMISSION,
                StringUtils.isEmpty(defaultPermission) ? AclPermission.READ.name() : defaultPermission);
        allContentObject.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());
        allObject.add(DefaultAclResourcesService.ALL, allContentObject);
        aclObject.add(DefaultAclResourcesService._ACL, allObject);
        return aclObject;
    }

    @Override
    public void refreshRegistry() {
        RequestParameters requestParameters = new RequestParametersBuilder(REGISTRY_DOMAIN).build();
        Response response = aclResourcesService.getCollection(getResmiGetRem(), adminsCollection, requestParameters,
                Collections.emptyList());

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

            Optional<JsonObject> collectionName = Optional.of(jsonElement).filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject).filter(jsonObject -> jsonObject.has(COLLECTION_NAME_FIELD));

            if (!collectionName.isPresent()) {
                LOG.error("Document in acl configuration collection has no collectionName field: {}", jsonElement.toString());
                continue;
            }

            Optional<String> collectionNameOptional = collectionName.map(jsonObject -> jsonObject.get(COLLECTION_NAME_FIELD))
                    .filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsJsonPrimitive).filter(JsonPrimitive::isString)
                    .map(JsonPrimitive::getAsString);

            if (!collectionNameOptional.isPresent()) {
                LOG.error("Unrecognized collectionName: {}", jsonElement.toString());
                continue;
            }

            addAclConfiguration(collectionNameOptional.get());

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

    private Rem getResmiPostRem() {
        if (resmiPostRem == null) {
            resmiPostRem = remService.getRem(RESMI_POST);
        }
        return resmiPostRem;
    }

    private Rem getResmiDeleteRem() {
        if (resmiDeleteRem == null) {
            resmiDeleteRem = remService.getRem(RESMI_DELETE);
        }
        return resmiDeleteRem;
    }

    @Override
    public void setRemService(RemService remService) {
        this.remService = remService;
    }

}
