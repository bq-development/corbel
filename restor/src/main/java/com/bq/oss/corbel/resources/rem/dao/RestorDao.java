package com.bq.oss.corbel.resources.rem.dao;

import com.bq.oss.corbel.resources.rem.model.RestorObject;
import org.springframework.http.MediaType;

/**
 * @author Alberto J. Rubio
 */
public interface RestorDao {

    RestorObject getObject(MediaType mediaType, String collection, String resource);

	void uploadObject(String collection, String resource, RestorObject object);

	void deleteObject(MediaType mediaType, String collection, String resource);
}
