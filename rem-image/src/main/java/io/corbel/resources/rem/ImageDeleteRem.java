package io.corbel.resources.rem;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.plugin.RestorRemNames;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.RemService;
import io.corbel.resources.rem.util.ImageRemUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageDeleteRem extends BaseRem<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(ImageDeleteRem.class);
    private final String cacheCollection;
    private final ImageRemUtil imageRemUtil;
    private RemService remService;

    public ImageDeleteRem(String cacheCollection, ImageRemUtil imageRemUtil) {
        this.cacheCollection = cacheCollection;
        this.imageRemUtil = imageRemUtil;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    @Override
    public Response collection(String collection, RequestParameters<CollectionParameters> requestParameters, URI uri,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = remService.getRem(RestorRemNames.RESTOR_DELETE);

        if (restorDeleteRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        restorDeleteRem.collection(collection, requestParameters, uri, Optional.empty());

        return Response.noContent().build();
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = remService.getRem(RestorRemNames.RESTOR_DELETE);

        if (restorDeleteRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        restorDeleteRem.collection(cacheCollection,
                imageRemUtil.getCollectionParametersWithPrefix(resourceId.getId(), requestParameters, cacheCollection), null,
                Optional.empty());
        restorDeleteRem.resource(collection, resourceId, requestParameters, Optional.empty());

        return Response.noContent().build();
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }
}
