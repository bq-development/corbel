package io.corbel.resources.rem.model;

import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Francisco Sanchez on 30/09/15.
 */
public class RestorInputStream extends InputStream {

    private final InputStream inputStream;
    private final S3Object s3Object;

    public RestorInputStream(S3Object s3Object) {
        this.inputStream = s3Object.getObjectContent();
        this.s3Object = s3Object;
    }
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    public void close() throws IOException {
        s3Object.close();
    }

}
