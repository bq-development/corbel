package com.bq.oss.corbel.resources.rem.dao;

import org.springframework.http.MediaType;

import com.google.common.base.Joiner;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultKeyNormalizer implements KeyNormalizer {

	public String normalize(MediaType mediaType, String collection, String resource) {
		return Joiner.on("/").join(collection, Joiner.on(".").join(resource, mediaType.toString().replace("/", "_")));
	}

}
