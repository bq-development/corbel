package io.corbel.resources.rem.restor.ioc;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.dao.DefaultKeyNormalizer;
import io.corbel.resources.rem.dao.KeyNormalizer;
import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.dao.S3RestorDao;
import io.corbel.resources.rem.health.S3HealthCheck;
import io.corbel.resources.rem.restor.RestorDeleteRem;
import io.corbel.resources.rem.restor.RestorGetRem;
import io.corbel.resources.rem.restor.RestorPutRem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alberto J. Rubio
 */
@Configuration
// import configuration mechanism
@Import({ ConfigurationIoC.class })
public class RestorIoc {

	private static final int AMAZONS3_RETRY_DEFAULT = 2;
	@Autowired
	private Environment env;

	@Bean(name = RestorIocBeanNames.RESTOR_GET)
	public Rem getRestorGetRem() throws Exception {
		return new RestorGetRem(getRestorDao());
	}

	@Bean(name = RestorIocBeanNames.RESTOR_PUT)
	public Rem getRestorPutRem() throws Exception {
		return new RestorPutRem(getRestorDao());
	}

	@Bean(name = RestorIocBeanNames.RESTOR_DELETE)
	public Rem getRestorDeleteRem() throws Exception {
		return new RestorDeleteRem(getRestorDao());
	}

	@Bean
	public RestorDao getRestorDao() {
		return new S3RestorDao(getKeyNormalizer(), getAmazonS3Client(), env.getProperty("restor.s3.bucket"),
				env.getProperty("restor.s3.retries", Integer.class, AMAZONS3_RETRY_DEFAULT));
	}

	@Bean
	public KeyNormalizer getKeyNormalizer() {
		return new DefaultKeyNormalizer();
	}

	@Bean
	public AmazonS3 getAmazonS3Client() {
		AmazonS3Client amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(env.getProperty("restor.s3.key"),
				env.getProperty("restor.s3.secret")));
		amazonS3Client.setRegion(Region.getRegion(Regions.fromName(env.getProperty("restor.s3.region"))));
		return amazonS3Client;
	}

	@Bean(name = RestorIocBeanNames.ACCEPTED_MEDIATYPES)
	public List<MediaType> getAcceptedMediaTypes() {
		List<MediaType> mediaTypes = new ArrayList<>();
		for (String mediaType : env.getProperty("restor.accepted.mediatypes").split(",")) {
			mediaTypes.add(MediaType.parseMediaType(mediaType));
		}
		return mediaTypes;
	}

	@Bean(name = RestorIocBeanNames.HEALTH_CHECK)
	public S3HealthCheck getS3HealthCheck() {
		return new S3HealthCheck(getAmazonS3Client(), env.getProperty("restor.s3.bucket"));
	}
}
