package io.corbel.resources.rem.resmi.ioc;

import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.mongo.IdInjectorMongoEventListener;
import io.corbel.lib.mongo.JsonObjectMongoReadConverter;
import io.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import io.corbel.lib.mongo.config.DefaultMongoConfiguration;
import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.JsonAggregationResultsFactory;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.dao.DefaultResmiOrder;
import io.corbel.resources.rem.dao.MongoResmiDao;
import io.corbel.resources.rem.dao.NamespaceNormalizer;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.dao.ResmiOrder;
import io.corbel.resources.rem.health.ElasticSearchHealthCheck;
import io.corbel.resources.rem.resmi.*;
import io.corbel.resources.rem.search.DefaultElasticSearchService;
import io.corbel.resources.rem.search.DefaultResmiSearch;
import io.corbel.resources.rem.search.ElasticSearchService;
import io.corbel.resources.rem.search.ResmiSearch;
import io.corbel.resources.rem.service.*;
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
import com.google.gson.JsonElement;

/**
 * @author Cristian del Cerro
 */
@Configuration// Import configuration mechanism
@Import({ConfigurationIoC.class, DefaultElasticSearchConfiguration.class})
public class ResmiIoc extends DefaultMongoConfiguration {

    @Value("${resmi.elasticsearch.index.settings:/elasticsearch/index.settings}")
    private String elasticSearchIndexSettings;
    @Value("${resmi.elasticsearch.defaultMapping.settings:/elasticsearch/defaultMapping.settings}")
    private String elasticSearchDefaultMappingSettings;
    @Value("${resmi.elasticsearch.enabled:false}")
    private Boolean elasticSearchEnabled;
    @Value("${resmi.textsearch.mode:disabled}")
    private String textSearchMode;

    @Autowired
    private Environment env;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected String getDatabaseName() {
        return "resmi";
    }

    @Bean
    public ResmiDao mongoResmiDao(AggregationResultsFactory<JsonElement> aggregationResultsFactory) throws Exception {
        return new MongoResmiDao(mongoTemplate(), getJsonObjectMongoWriteConverter(), getNamespaceNormilizer(), getMongoResmiOrder(),
                aggregationResultsFactory);
    }

    @Bean
    public ResmiOrder getMongoResmiOrder() throws Exception {
        return new DefaultResmiOrder(mongoTemplate(), getNamespaceNormilizer());
    }

    @Bean(name = ResmiRemNames.RESMI_GET)
    public Rem getResmiGetRem(ResmiService resmiService) throws Exception {
        return new ResmiGetRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_POST)
    public Rem getResmiPostRem(ResmiService resmiService) throws Exception {
        return new ResmiPostRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_PUT)
    public Rem getResmiPutRem(ResmiService resmiService) throws Exception {
        return new ResmiPutRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_DELETE)
    public Rem getResmiDeleteRem(ResmiService resmiService) throws Exception {
        return new ResmiDeleteRem(resmiService);
    }

    @Bean(name = ResmiRemNames.RESMI_INDEX)
    public Rem getResmiIndexRem(ResmiService resmiService) throws Exception {
        return new ResmiIndexRem(resmiService);
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
    public ResmiService resmiService(AggregationResultsFactory<JsonElement> aggregationResultsFactory, ResmiDao resmiDao) throws Exception {
        if (elasticSearchEnabled || textSearchMode.equalsIgnoreCase("elasticsearch")) {
            return new WithSearchResmiService(resmiDao, resmiSearch(aggregationResultsFactory), getSearchableFieldsRegistry(), getGson(), getClock());
        } else if (textSearchMode.equalsIgnoreCase("mongo")) {
            return new TextSearchResmiService(resmiDao, getClock());
        } else {
            return new DefaultResmiService(resmiDao, getClock());
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
    public ResmiSearch resmiSearch(AggregationResultsFactory<JsonElement> aggregationResultsFactory) {
        return new DefaultResmiSearch(getElasticeSearchService(), getNamespaceNormilizer(), elasticSearchIndexSettings,
                aggregationResultsFactory, getClock(), elasticSearchDefaultMappingSettings);
    }

    @Bean
    @Lazy
    public ElasticSearchService getElasticeSearchService() {
        return new DefaultElasticSearchService(applicationContext.getBean(Client.class), getGson());
    }

    @Bean(name = ResmiRemNames.ELASTICSEARCH_HEALTHCHECK)
    @Lazy
    public ElasticSearchHealthCheck getElasticSearchHealthCheck() {
        if(elasticSearchEnabled || textSearchMode.equalsIgnoreCase("elasticsearch")) {
            return new ElasticSearchHealthCheck(applicationContext.getBean(Client.class));
        } else {
            return new ElasticSearchHealthCheck();
        }
    }

    @Bean
    public AggregationResultsFactory<JsonElement> aggregationResultsFactory(Gson gson) {
        return new JsonAggregationResultsFactory(gson);
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
