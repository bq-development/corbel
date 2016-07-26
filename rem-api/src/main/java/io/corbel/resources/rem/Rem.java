package io.corbel.resources.rem;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.resources.rem.plugin.RemPlugin;
import io.corbel.resources.rem.request.*;

/**
 * REM: Resource resolver Module.
 * 
 * This modules are used to delegate the resolution of certain resource representations.
 * 
 * REMs must be registered via the {@link RemRegistry} during the execution of the {@link RemPlugin} implementation of this Rem.
 * 
 * @author Alexander De Leon
 * 
 */
public interface Rem<E> {

    Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<E> entity);

    Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<E> entity);

    Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, Optional<E> entity);

    default Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<E> entity, Optional<List<Rem>> excludedRems) {
        return collection(type, parameters, uri, entity);
    }

    default Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<E> entity, Optional<List<Rem>> excludedRems) {
        return resource(type, id, parameters, entity);
    }

    default Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, Optional<E> entity, Optional<List<Rem>> excludedRems) {
        return relation(type, id, relation, parameters, entity);
    }

    Class<E> getType();
}
