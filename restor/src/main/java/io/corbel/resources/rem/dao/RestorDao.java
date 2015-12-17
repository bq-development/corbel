package io.corbel.resources.rem.dao;

import io.corbel.resources.rem.model.RestorResourceUri;

import io.corbel.resources.rem.model.RestorObject;

/**
 * @author Alberto J. Rubio
 */
public interface RestorDao {

    RestorObject getObject(RestorResourceUri resourceUri);

    void uploadObject(RestorResourceUri resourceUri, RestorObject object);

    void deleteObject(RestorResourceUri resourceUri);

    void deleteObjectWithPrefix(RestorResourceUri resourceUri, String prefix);

}
