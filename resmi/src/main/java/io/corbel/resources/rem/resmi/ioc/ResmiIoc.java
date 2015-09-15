package io.corbel.resources.rem.resmi.ioc;

import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.mongo.IdInjectorMongoEventListener;
import io.corbel.lib.mongo.JsonObjectMongoReadConverter;
import io.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import io.corbel.lib.mongo.config.DefaultMongoConfiguration;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.dao.DefaultResmiOrder;
import io.corbel.resources.rem.dao.MongoResmiDao;
import io.corbel.resources.rem.dao.NamespaceNormalizer;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.dao.ResmiOrder;
import io.corbel.resources.rem.resmi.ResmiDeleteRem;
import io.corbel.resources.rem.resmi.ResmiGetRem;
import io.corbel.resources.rem.resmi.ResmiPostRem;
import io.corbel.resources.rem.resmi.ResmiPutRem;
import io.corbel.resources.rem.search.ElasticSearchResmiSearch;
import io.corbel.resources.rem.search.ElasticSearchService;
import io.corbel.resources.rem.search.ResmiSearch;
import io.corbel.resources.rem.service.DefaultNamespaceNormalizer;
import io.corbel.resources.rem.service.DefaultResmiService;
import io.corbel.resources.rem.service.InMemorySearchableFieldsRegistry;
import io.corbel.resources.rem.service.ResmiService;
import io.corbel.resources.rem.service.SearchableFieldsRegistry;
import io.corbel.resources.rem.service.WithSearchResmiService;
import io.corbel.resources.rem.utils.ResmiJsonObjectMongoWriteConverter;

import java.time.Clock;
import java.util.Arrays;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import com.google.gson.Gson;

/**
 * @author Cristian del Cerro
 */
@Configuration// Import configuration mechanism
@Import({ConfigurationIoC.class, DefaultElasticSearchConfiguration.class}) public class ResmiIoc extends DefaultMongoConfiguration {

    @Value("${resmi.elasticsearch.enabled:true}") private boolean elasticSearchEnabled;

    @Value("${resmi.elasticsearch.index.settings:/elasticsearch/index.settings}") private String elasticSearchIndexSettings;

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
        if (elasticSearchEnabled) {
            return new WithSearchResmiService(getMongoResmiDao(), getResmiSearch(), getSearchableFieldsRegistry(), getClock());
        } else {
            return new DefaultResmiService(getMongoResmiDao(), getClock());
        }
    }

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Lazy
    public SearchableFieldsRegistry getSearchableFieldsRegistry() {
        return new InMemorySearchableFieldsRegistry();
    }

    @Bean
    @Lazy
    public ResmiSearch getResmiSearch() {
        return new ElasticSearchResmiSearch(getElasticeSerachService(), getNamespaceNormilizer(), elasticSearchIndexSettings);
    }

    @Bean
    @Lazy
    public ElasticSearchService getElasticeSerachService() {
        return new ElasticSearchService(applicationContext.getBean(Client.class), getGson());
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
