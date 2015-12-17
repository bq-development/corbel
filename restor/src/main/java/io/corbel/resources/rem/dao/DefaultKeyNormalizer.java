package io.corbel.resources.rem.dao;

import com.google.common.base.Joiner;
import io.corbel.resources.rem.model.RestorResourceUri;

/**
 * @author Alexander De Leon
 */
public class DefaultKeyNormalizer implements KeyNormalizer {

    public String normalize(RestorResourceUri resourceUri) {
        String normalizedMediaType = resourceUri.getMediaType().replace("/", "_");
        String collection = resourceUri.getType();
        String resource = resourceUri.getTypeId();

        if (resource.endsWith(normalizedMediaType) && resource.startsWith(collection)) {
            return Joiner.on("/").join(resourceUri.getDomain(), resource);
        } else {
            return Joiner.on("/").join(resourceUri.getDomain(), collection, Joiner.on(".").join(resource, normalizedMediaType));
        }
    }
}
