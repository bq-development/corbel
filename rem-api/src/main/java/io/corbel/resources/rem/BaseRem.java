package io.corbel.resources.rem;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.resources.rem.model.Error;
import io.corbel.resources.rem.request.*;

/**
 * @author Alexander De Leon
 */
public abstract class BaseRem<E> implements Rem<E> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseRem.class);
    private static final Error DEFAULT_NOT_FOUND_ERROR = new Error("not_found", "Not found");

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<E> entity) {
        LOG.warn("Collections not implemented in this Rem: " + this.getClass().getName());
        return Response.status(Response.Status.NOT_FOUND).entity(DEFAULT_NOT_FOUND_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<E> entity) {
        LOG.warn("Resources not implemented in this Rem: " + this.getClass().getName());
        return Response.status(Response.Status.NOT_FOUND).entity(DEFAULT_NOT_FOUND_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
                             Optional<E> entity) {
        LOG.warn("Relations not implemented in this Rem: " + this.getClass().getName());
        return Response.status(Response.Status.NOT_FOUND).entity(DEFAULT_NOT_FOUND_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
