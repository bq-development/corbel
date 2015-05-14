package com.bq.oss.corbel.resources.rem.dao;

import org.springframework.http.MediaType;

/**
 * Normalizes namespaced labels into syntactic valid ids for Resources Storage.
 * 
 * @author Alberto J. Rubio
 * 
 */
public interface KeyNormalizer {

	String normalize(MediaType mediaType, String collection, String resource);

}
