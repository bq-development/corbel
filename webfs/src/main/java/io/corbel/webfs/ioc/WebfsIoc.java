package io.corbel.webfs.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.corbel.webfs.api.WebResource;
import io.corbel.webfs.service.AmazonS3Service;
import io.corbel.webfs.service.DefaultAmazonS3Service;
import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import io.corbel.lib.ws.cors.ioc.CorsIoc;
import io.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import io.corbel.lib.ws.dw.ioc.DropwizardIoc;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

/**
 * @author Rubén Carrasco
 *
 */

@Configuration @Import({ConfigurationIoC.class, CommonFiltersIoc.class, DropwizardIoc.class, CorsIoc.class, AuthorizationIoc.class}) public class WebfsIoc {

    @Autowired private Environment env;

    @Bean
    public WebResource getWebResource() {
        return new WebResource(getAmazonS3Service());
    }

    @Bean
    public AmazonS3Service getAmazonS3Service() {
        return new DefaultAmazonS3Service(getAmazonS3Client(), env.getProperty("webfs.s3.bucket"));
    }

    @Bean
    public AmazonS3 getAmazonS3Client() {
        AmazonS3Client amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(env.getProperty("webfs.s3.key"),
                env.getProperty("webfs.s3.secret")));
        amazonS3Client.setRegion(Region.getRegion(Regions.fromName(env.getProperty("webfs.s3.region"))));
        return amazonS3Client;
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

}
