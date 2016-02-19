package io.corbel.resources.rem.dao;

import io.corbel.resources.rem.model.RestorInputStream;
import io.corbel.resources.rem.model.RestorObject;
import io.corbel.resources.rem.model.RestorResourceUri;

import org.springframework.http.MediaType;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * @author Alberto J. Rubio
 */
public class S3RestorDao implements RestorDao {

    private final KeyNormalizer keyNormalizer;
    private final AmazonS3 amazonS3Client;
    private final String bucket;
    private final Integer amazonS3Retries;

    public S3RestorDao(KeyNormalizer keyNormalizer, AmazonS3 amazonS3Client, String bucket, Integer retry) {
        this.keyNormalizer = keyNormalizer;
        this.amazonS3Client = amazonS3Client;
        this.bucket = bucket;
        this.amazonS3Retries = retry;
    }

    @Override
    public RestorObject getObject(RestorResourceUri resourceUri) {
        return getObject(resourceUri, 0);
    }

    private RestorObject getObject(RestorResourceUri resourceUri, int retryNumber) {
        GetObjectRequest objectRequest = new GetObjectRequest(bucket, keyNormalizer.normalize(resourceUri));
        try {
            S3Object s3Object = amazonS3Client.getObject(objectRequest);
            String objectType = MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()).toString();
            return new RestorObject(objectType, new RestorInputStream(s3Object), s3Object.getObjectMetadata().getContentLength(), s3Object
                    .getObjectMetadata().getETag());
        } catch (AmazonS3Exception e) {
            switch (e.getStatusCode()) {
                case 404:
                    return null;
                case 500:
                    if (retryNumber > amazonS3Retries) {
                        return getObject(resourceUri, retryNumber + 1);
                    }
                default:
                    throw e;
            }
        }
    }

    @Override
    public void uploadObject(RestorResourceUri resourceUri, RestorObject object) {
        PutObjectRequest objectRequest = new PutObjectRequest(bucket, keyNormalizer.normalize(resourceUri), object.getInputStream(),
                createObjectMetadataForObject(object.getMediaType(), object.getContentLength()));
        amazonS3Client.putObject(objectRequest);
    }

    @Override
    public void deleteObject(RestorResourceUri resourceUri) {
        deleteObject(keyNormalizer.normalize(resourceUri));
    }

    private void deleteObject(String key) {
        deleteObject(key, 0);
    }

    @Override
    public void deleteObjectWithPrefix(RestorResourceUri resourceUri, String prefix) {
        ObjectListing objectListing = amazonS3Client.listObjects(new ListObjectsRequest().withBucketName(bucket).withPrefix(
                keyNormalizer.normalize(resourceUri, prefix)));
        boolean truncated;

        do {
            objectListing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).forEach(this::deleteObject);

            if (truncated = objectListing.isTruncated()) {
                objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
            }
        } while (truncated);
    }

    private void deleteObject(String key, int retryNumber) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, key));
        } catch (AmazonS3Exception e) {
            switch (e.getStatusCode()) {
                case 500:
                    if (retryNumber > amazonS3Retries) {
                        deleteObject(key, retryNumber + 1);
                        break;
                    }
                default:
                    throw e;
            }
        }
    }

    private ObjectMetadata createObjectMetadataForObject(String mediaType, Long contentLength) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mediaType);
        if (contentLength != null) {
            objectMetadata.setContentLength(contentLength);
        }
        return objectMetadata;
    }

}
