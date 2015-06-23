package com.bq.oss.corbel.resources.rem.service;

import java.io.*;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.RequestParametersImplCustomContentLength;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.google.common.base.Joiner;

public class DefaultImageCacheService implements ImageCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultImageCacheService.class);
    public String cacheCollection;

    public DefaultImageCacheService(String cacheCollection) {
        this.cacheCollection = cacheCollection;
    }

    @Override
    public InputStream getFromCache(Rem<?> restorRem, ResourceId resourceId, String operationsChain, String collection,
            RequestParameters<ResourceParameters> parameters) {

        resourceId = generateId(resourceId, collection, operationsChain);
        Response response = restorRem.resource(cacheCollection, resourceId, parameters, Optional.empty());
        if (response.getStatus() == 200 && response.getEntity() != null) {
            return (InputStream) response.getEntity();
        }
        return null;
    }

    @Override
    @Async
    public void saveInCacheAsync(Rem<InputStream> restorPutRem, ResourceId resourceId, String operationsChain, Long newSize,
            String collection, RequestParameters<ResourceParameters> parameters, File file) {
        try (InputStream inputStream = createInputStream(file)) {
            resourceId = generateId(resourceId, collection, operationsChain);
            RequestParameters<ResourceParameters> newParameters = new RequestParametersImplCustomContentLength<>(parameters, newSize);
            restorPutRem.resource(cacheCollection, resourceId, newParameters, Optional.of(inputStream));
            file.delete();
        } catch (IOException e) {
            LOG.error("Error while saving image in cache", e);
        }
    }

    @Override
    public void invalidateCache(Rem<InputStream> restorDeleteRem, ResourceId resourceId) {

    }

    FileInputStream createInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private ResourceId generateId(ResourceId resourceId, String collection, String operationsChain) {
        return new ResourceId(Joiner.on('.').join("cachedImage", resourceId.getId(), collection, operationsChain));
    }

}
