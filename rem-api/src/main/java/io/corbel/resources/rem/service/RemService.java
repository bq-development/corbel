package io.corbel.resources.rem.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.model.RemDescription;
import io.corbel.resources.rem.request.*;

/**
 * @author Alberto J. Rubio
 */
public interface RemService {

    Rem getRem(String name);

    Rem<?> getRem(String type, List<MediaType> acceptedMediaTypes, HttpMethod method);

    Rem<?> getRem(String type, List<MediaType> acceptedMediaTypes, HttpMethod method, List<Rem> remsExcluded);

    void registerExcludedRems(String uri, List<Rem> excludedRems);

    void registerRem(Rem rem, String uriPattern, HttpMethod httpMethod);

    void unregisterRem(Class<?> remClass, String uriPattern);

    default Response collection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional entity) {
        return collection(rem, type, parameters, uri, entity, null);
    }

    default Response resource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional entity) {
        return resource(rem, type, id, parameters, entity, null);
    }

    default Response relation(Rem rem, String type, ResourceId id, String rel, RequestParameters<RelationParameters> parameters, Optional entity) {
        return relation(rem, type, id, rel, parameters, entity, null);
    }

    default Response collection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional entity, List<Rem> remsExcluded) {
        return collection(rem, type, parameters, uri, entity);
    }

    default Response resource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional entity, List<Rem> remsExcluded) {
        return resource(rem, type, id, parameters, entity);
    }

    default Response relation(Rem rem, String type, ResourceId id, String rel, RequestParameters<RelationParameters> parameters, Optional entity, List<Rem> remsExcluded) {
        return relation(rem, type, id, rel, parameters, entity);
    }

    List<RemDescription> getRegisteredRemDescriptions();
}
