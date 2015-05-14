package com.bq.oss.corbel.resources.rem;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.plugin.RemPlugin;
import com.bq.oss.corbel.resources.rem.request.*;

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

    Class<E> getType();
}
