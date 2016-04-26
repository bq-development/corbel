package io.corbel.webfs.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

/**
 * @author Rubén Carrasco
 *
 */
public class DefaultAmazonS3Service implements AmazonS3Service {

    private final AmazonS3 amazonS3Client;
    private final String bucket;

    public DefaultAmazonS3Service(AmazonS3 amazonS3Client, String bucket) {
        this.amazonS3Client = amazonS3Client;
        this.bucket = bucket;
    }

    @Override
    public S3Object getObject(String key) {

        GetObjectRequest objectRequest = new GetObjectRequest(bucket, key);


        try {
            return amazonS3Client.getObject(objectRequest);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public void deleteFolder(String path){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(path);
        ObjectListing objectListing;

        do {
            objectListing = amazonS3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary :objectListing.getObjectSummaries()) {
                amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, objectSummary.getKey()));
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
    }
}
