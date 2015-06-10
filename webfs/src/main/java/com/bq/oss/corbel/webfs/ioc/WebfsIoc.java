package com.bq.oss.corbel.webfs.ioc;

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
import com.bq.oss.corbel.webfs.api.WebResource;
import com.bq.oss.corbel.webfs.service.AmazonS3Service;
import com.bq.oss.corbel.webfs.service.DefaultAmazonS3Service;
import com.bq.oss.lib.config.ConfigurationIoC;
import com.bq.oss.lib.ws.auth.ioc.AuthorizationIoc;
import com.bq.oss.lib.ws.cors.ioc.CorsIoc;
import com.bq.oss.lib.ws.dw.ioc.CommonFiltersIoc;
import com.bq.oss.lib.ws.dw.ioc.DropwizardIoc;

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

}
