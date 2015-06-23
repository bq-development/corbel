package com.bq.oss.corbel.resources.rem;

import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;

public class ImagePutRem extends BaseRem<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(ImagePutRem.class);

    private final RemService remService;
    private final String cacheCollection;
    private final ImageRemUtil imageRemUtil;

    public ImagePutRem(RemService remService, String cacheCollection, ImageRemUtil imageRemUtil) {
        this.remService = remService;
        this.cacheCollection = cacheCollection;
        this.imageRemUtil = imageRemUtil;
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<InputStream> entity) {

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorDeleteRem = (Rem<InputStream>) remService.getRem(collection, requestParameters.getAcceptedMediaTypes(),
                HttpMethod.DELETE, Collections.singletonList(this));

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorPutRem = (Rem<InputStream>) remService.getRem(collection, requestParameters.getAcceptedMediaTypes(),
                HttpMethod.PUT, Collections.singletonList(this));

        if (restorDeleteRem == null || restorPutRem == null) {
            LOG.warn("RESTOR not found. May  be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        restorDeleteRem.collection(cacheCollection, imageRemUtil.getCollectionParametersWithPrefix(resourceId.getId(), requestParameters),
                null, Optional.empty());
        restorPutRem.resource(collection, resourceId, requestParameters, entity);

        return Response.noContent().build();
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }
}
