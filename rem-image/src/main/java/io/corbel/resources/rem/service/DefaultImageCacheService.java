package io.corbel.resources.rem.service;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.RequestParametersImplCustomContentLength;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Optional;

public class DefaultImageCacheService implements ImageCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultImageCacheService.class);
    private String cacheCollection;

    public DefaultImageCacheService(String cacheCollection) {
        this.cacheCollection = cacheCollection;
    }

    @Override
    public InputStream getFromCache(Rem<?> restorRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat, String collection,
                                    RequestParameters<ResourceParameters> parameters) {

        resourceId = generateId(resourceId, collection, operationsChain, imageFormat);
        Response response = restorRem.resource(cacheCollection, resourceId, parameters, Optional.empty());
        if (response.getStatus() == 200 && response.getEntity() != null) {
            return (InputStream) response.getEntity();
        }
        return null;
    }

    @Override
    @Async
    public void saveInCacheAsync(Rem<InputStream> restorPutRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat, Long newSize,
                                 String collection, RequestParameters<ResourceParameters> parameters, File file) {
        try (InputStream inputStream = createInputStream(file)) {
            resourceId = generateId(resourceId, collection, operationsChain, imageFormat);
            parameters = new RequestParametersImplCustomContentLength(parameters, newSize);
            restorPutRem.resource(cacheCollection, resourceId, parameters, Optional.of(inputStream));
            file.delete();
        } catch (IOException e) {
            LOG.error("Error while saving image in cache", e);
        }
    }

    protected FileInputStream createInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private ResourceId generateId(ResourceId resourceId, String collection, String operationsChain, Optional<ImageFormat> imageFormat) {
        if(imageFormat.isPresent()){
            return new ResourceId(Joiner.on(".").join(resourceId.getId(), collection, operationsChain, imageFormat.get()));
        }else{
            return new ResourceId(Joiner.on(".").join(resourceId.getId(), collection, operationsChain));
        }
    }
}
