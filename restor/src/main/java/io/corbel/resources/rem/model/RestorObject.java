package io.corbel.resources.rem.model;

import java.io.InputStream;

/**
 * @author Alberto J. Rubio
 */
public class RestorObject {

    private final String mediaType;
    private final InputStream inputStream;
    private final Long contentLength;
    private final String etag;

    public RestorObject(String mediaType, InputStream inputStream, Long contentLength, String etag) {
        this.mediaType = mediaType;
        this.inputStream = inputStream;
        this.contentLength = contentLength;
        this.etag = etag;
    }


    public RestorObject(String mediaType, InputStream inputStream, Long contentLength) {
        this.mediaType = mediaType;
        this.inputStream = inputStream;
        this.contentLength = contentLength;
        this.etag = null;
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

    public String getEtag() {
        return etag;
    }

}
