package io.corbel.resources.rem;

import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.plugin.RestorRemNames;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.ImageCacheService;

public class ImagePutRem extends ImageBaseRem {

    private static final Logger LOG = LoggerFactory.getLogger(ImagePutRem.class);
    private final ImageCacheService imageCacheService;

    public ImagePutRem(ImageCacheService imageCacheService) {
        this.imageCacheService = imageCacheService;
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = remService.getRem(RestorRemNames.RESTOR_DELETE);

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorPutRem = remService.getRem(RestorRemNames.RESTOR_PUT);

        if (restorDeleteRem == null || restorPutRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }


        imageCacheService.removeFromCache(restorDeleteRem, requestParameters, resourceId, collection);
        restorPutRem.resource(collection, resourceId, requestParameters, entity);

        return Response.noContent().build();
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }
}
