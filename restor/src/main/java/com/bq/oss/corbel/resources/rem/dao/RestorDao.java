package com.bq.oss.corbel.resources.rem.dao;

import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.model.RestorObject;

/**
 * @author Alberto J. Rubio
 */
public interface RestorDao {

    RestorObject getObject(MediaType mediaType, String collection, String resource);

    void uploadObject(String collection, String resource, RestorObject object);

    void deleteObject(MediaType mediaType, String collection, String resource);

    void deleteObjectWithPrefix(MediaType mediaType, String collection, String prefix);

}
