package io.corbel.resources.rem.model;

import java.io.InputStream;

import org.springframework.http.MediaType;

/**
 * @author Alberto J. Rubio
 */
public class RestorObject {

	private final String mediaType;
	private final InputStream inputStream;
	private final Long contentLength;

	public RestorObject(String mediaType, InputStream inputStream, Long contentLength) {
		this.mediaType = mediaType;
		this.inputStream = inputStream;
		this.contentLength = contentLength;
	}

	public String getMediaType() {
		return mediaType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public Long getContentLength() {
		return contentLength;
	}

}
