package com.bq.oss.corbel.resources.rem.resmi.ioc;

import java.time.Clock;
import java.util.Arrays;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.dao.*;
import com.bq.oss.corbel.resources.rem.resmi.ResmiDeleteRem;
import com.bq.oss.corbel.resources.rem.resmi.ResmiGetRem;
import com.bq.oss.corbel.resources.rem.resmi.ResmiPostRem;
import com.bq.oss.corbel.resources.rem.resmi.ResmiPutRem;
import com.bq.oss.corbel.resources.rem.search.DummyResmiSearch;
import com.bq.oss.corbel.resources.rem.search.ElasticSearchResmiSearch;
import com.bq.oss.corbel.resources.rem.search.ResmiSearch;
import com.bq.oss.corbel.resources.rem.service.*;
import com.bq.oss.corbel.resources.rem.utils.ResmiJsonObjectMongoWriteConverter;
import com.bq.oss.lib.config.ConfigurationIoC;
import com.bq.oss.lib.mongo.IdInjectorMongoEventListener;
import com.bq.oss.lib.mongo.JsonObjectMongoReadConverter;
import com.bq.oss.lib.mongo.JsonObjectMongoWriteConverter;
import com.bq.oss.lib.mongo.config.DefaultMongoConfiguration;
import com.google.gson.Gson;

/**
 * @author Cristian del Cerro
 */
@Configuration// Import configuration mechanism
@Import({ConfigurationIoC.class, DefaultElasticSearchConfiguration.class}) public class ResmiIoc extends DefaultMongoConfiguration {

    @Value("${resmi.elasticsearch.enabled:true}") private boolean elasticSearchEnabled;
    @Value("${resmi.elasticsearch.stopwords:}") private String elasticSearchStopWords;

    @Autowired private Environment env;

    @Override
    protected String getDatabaseName() {
        return "resmi";
    }

    @Autowired private ApplicationContext applicationContext;

    @Bean
    public ResmiDao getMongoResmiDao() throws Exception {
        return new MongoResmiDao(mongoTemplate(), getJsonObjectMongoWriteConverter(), getNamespaceNormilizer(), getMongoResmiOrder());
    }

    @Bean
    public ResmiOrder getMongoResmiOrder() throws Exception {
        return new DefaultResmiOrder(mongoTemplate(), getNamespaceNormilizer());
    }

    @Bean(name = ResmiRemNames.RESMI_GET)
    public Rem getResmiGetRem() throws Exception {
        return new ResmiGetRem(getResmiService());
    }

    @Bean(name = ResmiRemNames.RESMI_POST)
    public Rem getResmiPostRem() throws Exception {
        return new ResmiPostRem(getResmiService());
    }

    @Bean(name = ResmiRemNames.RESMI_PUT)
    public Rem getResmiPutRem() throws Exception {
        return new ResmiPutRem(getResmiService());
    }

    @Bean(name = ResmiRemNames.RESMI_DELETE)
    public Rem getResmiDeleteRem() throws Exception {
        return new ResmiDeleteRem(getResmiService());
    }

    @Bean
    public IdInjectorMongoEventListener getIdInjectorMongoEventListener() {
        return new IdInjectorMongoEventListener();
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(getJsonObjectMongoReadConverter(), getJsonObjectMongoWriteConverter()));
    }

    @Bean
    public JsonObjectMongoReadConverter getJsonObjectMongoReadConverter() {
        return new JsonObjectMongoReadConverter(getGson());
    }

    @Bean
    public JsonObjectMongoWriteConverter getJsonObjectMongoWriteConverter() {
        return new ResmiJsonObjectMongoWriteConverter();
    }

    @Bean
    public ResmiService getResmiService() throws Exception {
        return new DefaultResmiService(getMongoResmiDao(), getSearchClient(), getSearchableFieldsRegistry(), getClock());
    }

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    public SearchableFieldsRegistry getSearchableFieldsRegistry() {
        return new InMemorySearchableFieldsRegistry();
    }

    @Bean
    public ResmiSearch getSearchClient() {
        if (elasticSearchEnabled) {
            return new ElasticSearchResmiSearch(applicationContext.getBean(Client.class), getNamespaceNormilizer(),
                    getGson(), elasticSearchStopWords.split(","));
        } else {
            return new DummyResmiSearch();
        }
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public NamespaceNormalizer getNamespaceNormilizer() {
        return new DefaultNamespaceNormalizer();
    }
}
