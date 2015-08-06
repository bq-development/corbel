package io.corbel.resources.rem.health;

import com.amazonaws.services.s3.AmazonS3;
import com.codahale.metrics.health.HealthCheck;

public class S3HealthCheck extends HealthCheck {

	private final AmazonS3 amazonS3Client;
	private final String bucket;

	public S3HealthCheck(AmazonS3 amazonS3Client, String bucket) {
		this.amazonS3Client = amazonS3Client;
		this.bucket = bucket;
	}

	@Override
	protected Result check() throws Exception {
		if (amazonS3Client.doesBucketExist(bucket)) {
			return Result.healthy();
		} else {
			return Result.unhealthy("Bucket " + bucket + " not exists");
		}
	}

}
