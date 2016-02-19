package io.corbel.resources.rem.dao;

import io.corbel.resources.rem.model.RestorResourceUri;

/**
 * Normalizes namespaced labels into syntactic valid ids for Resources Storage.
 * 
 * @author Alberto J. Rubio
 * 
 */
public interface KeyNormalizer {

    String normalize(RestorResourceUri uri);

    String normalize(RestorResourceUri uri, String prefix);

}
