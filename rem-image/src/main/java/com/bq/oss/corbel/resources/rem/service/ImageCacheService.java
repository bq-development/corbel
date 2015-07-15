package com.bq.oss.corbel.resources.rem.service;

import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.format.ImageFormat;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public interface ImageCacheService {

    InputStream getFromCache(Rem<?> restorRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat, String type,
                             RequestParameters<ResourceParameters> parameters);

    void saveInCacheAsync(Rem<InputStream> restorPutRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat, Long newSize, String collection,
                          RequestParameters<ResourceParameters> parameters, File file);

}
