package com.bq.oss.corbel.webfs.service;

import com.amazonaws.services.s3.model.S3Object;

/**
 * @author Rubén Carrasco
 *
 */
public interface AmazonS3Service {

    public S3Object getObject(String uri);

}
