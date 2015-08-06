package io.corbel.resources.rem.restor;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;

/**
 * @author Rubén Carrasco
 */
public class RestorDeleteRem extends AbstractRestorRem {

    private static final String PREFIX_PARAMETER_NAME = "prefix";

    public RestorDeleteRem(RestorDao dao) {
        super(dao);
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {
        return Optional.ofNullable(parameters.getCustomParameterValue(PREFIX_PARAMETER_NAME)).map(prefix -> {
            dao.deleteObjectWithPrefix(getMediaType(parameters), type, prefix);
            return Response.noContent().build();
        }).orElseGet(() -> super.collection(type, parameters, uri, entity));
    }

    @Override
    public Response resource(String collection, ResourceId resource, RequestParameters<ResourceParameters> parameters,
            Optional<InputStream> entity) {
        dao.deleteObject(getMediaType(parameters), collection, resource.getId());
        return Response.noContent().build();
    }

}
