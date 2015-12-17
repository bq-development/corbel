package io.corbel.resources.rem.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpMethod;

import com.google.gson.JsonObject;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.*;

/**
 * @author Cristian del Cerro
 */
public interface AclResourcesService {

    void setRemsAndMethods(List<Pair<Rem, HttpMethod>> remsAndMethods);

    Response saveResource(Rem rem, RequestParameters<CollectionParameters> parameters, String type, URI uri, Object entity, List<Rem> excludedRems);

    Response getResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, List<Rem> excludedRems);

    Response getCollection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, List<Rem> excludedRems);

    Response getRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, List<Rem> excludedRems);

    Response updateResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Object entity, List<Rem> excludedRems);

    Response putRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Object entity, List<Rem> excludedRems);

    Response deleteResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, List<Rem> excludedRems);

    Response deleteRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, List<Rem> excludedRems);

    boolean isAuthorized(String requestedDomain, TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException;

    boolean isManagedBy(String requestDomain, TokenInfo tokenInfo, String type);

    Optional<JsonObject> getResourceIfIsAuthorized(String requestedDomain, TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException;

    Response updateConfiguration(ResourceId id, ManagedCollection managedCollection);

    void addAclConfiguration(String collection);

    void removeAclConfiguration(String collection);

    void refreshRegistry();

    void setRemService(RemService remService);

}
