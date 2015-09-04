package io.corbel.oauth.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import io.corbel.lib.mongo.config.DefaultMongoConfiguration;
import io.corbel.lib.mongo.config.MongoCommonRepositoryFactoryBean;
import io.corbel.lib.ws.dw.ioc.MongoHealthCheckIoc;
import io.corbel.oauth.repository.MongoIndexes;

/**
 * @author Alexander De Leon
 *
 */
@Configuration @Import({MongoHealthCheckIoc.class}) @EnableMongoRepositories(value = {"io.corbel.oauth.repository"},
        repositoryFactoryBeanClass = MongoCommonRepositoryFactoryBean.class) public class OauthMongoIoc extends DefaultMongoConfiguration {

    @Autowired private Environment env;

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected String getDatabaseName() {
        return env.getProperty("oauth.mongodb.database");
    }

    @Override
    protected String getMongoConfigurationPrefix() {
        return "oauth";
    }

    @Bean
    @Lazy(false)
    public MongoIndexes getMongoIndexes() {
        return new MongoIndexes();
    }
}
