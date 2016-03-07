package io.corbel.resources.rem.service;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

/**
 * @author Cristian del Cerro
 */
public interface AclResourcesService {

    Response saveResource(Rem rem, RequestParameters<CollectionParameters> parameters, String type, URI uri, Object entity,
            List<Rem> excludedRems);

    Response getResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, List<Rem> excludedRems);

    Response getCollection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, List<Rem> excludedRems);

    Response getRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            List<Rem> excludedRems);

    Response updateResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Object entity,
            List<Rem> excludedRems);

    Response putRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            Object entity, List<Rem> excludedRems);

    Response deleteResource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, List<Rem> excludedRems);

    Response deleteRelation(Rem rem, String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
            List<Rem> excludedRems);

    boolean isAuthorized(String requestedDomain, TokenInfo tokenInfo, String type, ResourceId resourceId, AclPermission operation)
            throws AclFieldNotPresentException;

    Optional<JsonObject> getResourceIfIsAuthorized(String requestedDomain, TokenInfo tokenInfo, String type, ResourceId resourceId,
            AclPermission operation) throws AclFieldNotPresentException;

    void setRemService(RemService remService);

    boolean isManagedBy(String requestedDomain, TokenInfo tokenInfo, String collection);

}
