package io.corbel.resources.rem.service;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;

public interface ImageCacheService {

    InputStream getFromCache(Rem<?> restorRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat,
            String type, RequestParameters<ResourceParameters> parameters);

    void saveInCacheAsync(Rem<InputStream> restorPutRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat,
            Long newSize, String collection, RequestParameters<ResourceParameters> parameters, File file);

    void removeFromCache(Rem<InputStream> restorDeleteRem, RequestParameters<ResourceParameters> parameters, ResourceId resourceId,
            String collection);

    void removeFromCollectionCache(Rem<InputStream> restorDeleteRem, RequestParameters<CollectionParameters> parameters, String collection);

}
