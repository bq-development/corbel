package io.corbel.resources.rem.restor;

import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.model.RestorResourceUri;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.model.RestorObject;

/**
 * @author Rubén Carrasco
 */
public class RestorPutRem extends AbstractRestorRem {

    public RestorPutRem(RestorDao dao) {
        super(dao);
    }

    @Override
    public Response resource(String collection, ResourceId resource, RequestParameters<ResourceParameters> parameters,
            Optional<InputStream> entity) {
        if (!entity.isPresent()) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
        try {
            String encoding = parameters.getCustomParameterValue("resource:encoding");
            InputStream stream = entity.get();
            Long contentLength = parameters.getContentLength();
            if (encoding != null && "base64".equals(encoding)) {
                String length = parameters.getCustomParameterValue("resource:length");
                if (length == null) {
                    return ErrorResponseFactory.getInstance().badRequest();
                }
                contentLength = Long.valueOf(length);
                stream = Base64.getDecoder().wrap(entity.get());
            }
            RestorResourceUri resourceUri = new RestorResourceUri(parameters.getRequestedDomain(), getMediaType(parameters), collection, resource.getId());

            dao.uploadObject(resourceUri, new RestorObject(getMediaType(parameters), stream, contentLength));
            return Response.noContent().build();
        } catch (NumberFormatException e) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
    }
}
