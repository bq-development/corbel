package io.corbel.resources.rem.service;

import java.io.*;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.request.*;

public class DefaultImageCacheService implements ImageCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultImageCacheService.class);
    private static final String PREFIX = "prefix";
    public String cacheCollection;

    public DefaultImageCacheService(String cacheCollection) {
        this.cacheCollection = cacheCollection;
    }

    @Override
    public InputStream getFromCache(Rem<?> restorRem, ResourceId resourceId, String operationsChain, Optional<ImageFormat> imageFormat,
            String collection, RequestParameters<ResourceParameters> parameters) {

        resourceId = generateId(resourceId, collection, operationsChain, imageFormat);
        Response response = restorRem.resource(cacheCollection, resourceId, parameters, Optional.empty());
        if (response.getStatus() == 200 && response.getEntity() != null) {
            return (InputStream) response.getEntity();
        }
        return null;
    }

    @Override
    @Async
    public void saveInCacheAsync(Rem<InputStream> restorPutRem, ResourceId resourceId, String operationsChain,
            Optional<ImageFormat> imageFormat, Long newSize, String collection, RequestParameters<ResourceParameters> parameters,
            File file) {
        try (InputStream inputStream = createInputStream(file)) {
            resourceId = generateId(resourceId, collection, operationsChain, imageFormat);
            parameters = new RequestParametersImplCustomContentLength(parameters, newSize);
            restorPutRem.resource(cacheCollection, resourceId, parameters, Optional.of(inputStream));

            if (!file.delete()) {
                LOG.warn("Error deleting {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            LOG.error("Error while saving image in cache", e);
        }
    }

    @Override
    public void removeFromCollectionCache(Rem<InputStream> restorDeleteRem, RequestParameters<CollectionParameters> parameters,
            String collection) {
        restorDeleteRem.collection(cacheCollection, getCollectionParameters(getPrefix(collection), parameters), null, Optional.empty());
    }

    @Override
    public void removeFromCache(Rem<InputStream> restorDeleteRem, RequestParameters<ResourceParameters> parameters, ResourceId resourceId,
            String collection) {
        restorDeleteRem.collection(cacheCollection, getCollectionParameters(getPrefix(resourceId.getId(), collection), parameters), null,
                Optional.empty());
    }

    private <T> RequestParameters<CollectionParameters> getCollectionParameters(String prefix, RequestParameters<T> parameters) {
        MultivaluedMap<String, String> newParameters = new MultivaluedHashMap<String, String>(parameters.getParams());
        newParameters.putSingle(PREFIX, prefix);

        return new RequestParametersImpl<>(
                new CollectionParametersImpl(new Pagination(1, 10), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty()),
                parameters.getTokenInfo(), parameters.getRequestedDomain(), parameters.getAcceptedMediaTypes(),
                parameters.getContentLength(), newParameters, parameters.getHeaders());
    }

    protected FileInputStream createInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private ResourceId generateId(ResourceId resourceId, String collection, String operationsChain, Optional<ImageFormat> imageFormat) {
        String id = getPrefix(resourceId.getId(), collection) + operationsChain;
        return new ResourceId(imageFormat.map(imgFormat -> id + '.' + imgFormat).orElse(id));
    }

    private String getPrefix(String collection) {
        return collection + '.';
    }

    private String getPrefix(String resourceId, String collection) {
        return collection + '.' + resourceId + '.';
    }

}
