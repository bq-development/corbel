package io.corbel.resources.ioc;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.ContainerRequestFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import io.corbel.eventbus.ioc.EventBusIoc;
import io.corbel.eventbus.service.EventBus;
import io.corbel.rem.internal.DefaultRemEntityTypeResolver;
import io.corbel.rem.internal.InMemoryHealthCheckRegistry;
import io.corbel.rem.internal.InMemoryPluginArtifactIdRegistry;
import io.corbel.rem.internal.InMemoryRelationRegistry;
import io.corbel.rem.internal.InMemoryRemRegistry;
import io.corbel.rem.internal.RemEntityTypeResolver;
import io.corbel.resources.api.PluginInfoResource;
import io.corbel.resources.api.RemResource;
import io.corbel.resources.href.DefaultLinkGenerator;
import io.corbel.resources.href.LinkGenerator;
import io.corbel.resources.href.LinksFilter;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.model.Mode;
import io.corbel.resources.rem.plugin.HealthCheckRegistry;
import io.corbel.resources.rem.plugin.PluginArtifactIdRegistry;
import io.corbel.resources.rem.plugin.RelationRegistry;
import io.corbel.resources.rem.service.RemService;
import io.corbel.resources.repository.RelationSchemaRepository;
import io.corbel.resources.service.DefaultRelationSchemaService;
import io.corbel.resources.service.DefaultRemService;
import io.corbel.resources.service.DefaultResourcesService;
import io.corbel.resources.service.RelationSchemaService;
import io.corbel.resources.service.ResourcesService;
import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.mongo.config.DefaultMongoConfiguration;
import io.corbel.lib.queries.builder.QueryParametersBuilder;
import io.corbel.lib.queries.mongo.repository.QueriesRepositoryFactoryBean;
import io.corbel.lib.queries.parser.AggregationParser;
import io.corbel.lib.queries.parser.CustomJsonParser;
import io.corbel.lib.queries.parser.CustomSearchParser;
import io.corbel.lib.queries.parser.DefaultPaginationParser;
import io.corbel.lib.queries.parser.JacksonAggregationParser;
import io.corbel.lib.queries.parser.JacksonQueryParser;
import io.corbel.lib.queries.parser.JacksonSortParser;
import io.corbel.lib.queries.parser.PaginationParser;
import io.corbel.lib.queries.parser.QueryParser;
import io.corbel.lib.queries.parser.SearchParser;
import io.corbel.lib.queries.parser.SortParser;
import io.corbel.lib.token.ioc.OneTimeAccessTokenIoc;
import io.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import io.corbel.lib.ws.cors.ioc.CorsIoc;
import io.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import io.corbel.lib.ws.dw.ioc.DropwizardIoc;
import io.corbel.lib.ws.dw.ioc.MongoHealthCheckIoc;
import io.corbel.lib.ws.encoding.MatrixEncodingRequestFilter;
import io.corbel.lib.ws.ioc.QueriesIoc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;


/**
 * @author Alexander De Leon
 */
@Configuration// import configuration mechanism
@Import({ConfigurationIoC.class, CommonFiltersIoc.class, DropwizardIoc.class, OneTimeAccessTokenIoc.class, AuthorizationIoc.class,
        CorsIoc.class, QueriesIoc.class, MongoHealthCheckIoc.class, EventBusIoc.class})// scan package to discover REM implementations
@ComponentScan({"io.corbel.resources.cli.dsl", "com.bqreaders.silkroad.resources.cli.dsl",
        "io.corbel.resources.rem.plugin", "com.bqreaders.silkroad.resources.rem.plugin"}) @EnableMongoRepositories(
        value = "io.corbel.resources.repository", repositoryFactoryBeanClass = QueriesRepositoryFactoryBean.class) @EnableCaching @SuppressWarnings("unused") public class ResourcesIoc
        extends DefaultMongoConfiguration {

    @Autowired private Environment env;

    @Autowired private RelationSchemaRepository relationSchemaRepository;


    // This bean fixed the problem with nginx sending the matrix param in a non encoded form.
    @Bean
    public ContainerRequestFilter matrixEncodingFilter() {
        return new MatrixEncodingRequestFilter("^(.*/v1.0/.+/resource/.+/.+/.+;r=)(.+)$");
    }

    @Bean
    public RemResource getRemResource(ResourcesService resourcesService) {
        return new RemResource(resourcesService);
    }

    @Bean
    public ResourcesService getResourcesService(RemService remService, RemEntityTypeResolver remEntityTypeResolver,
            QueryParametersBuilder queryParametersBuilder, EventBus eventBus) {
        return new DefaultResourcesService(remService, remEntityTypeResolver, getPageSizeDefault(), getMaxPageSizeDefault(),
                queryParametersBuilder, eventBus);
    }

    @Bean
    public RemService getRemService(RemRegistry remRegistry) {
        return new DefaultRemService(remRegistry);
    }

    @Bean
    @SuppressWarnings("unused")
    public RelationSchemaService getRelationSchemaService(RelationRegistry relationRegistry) {
        return new DefaultRelationSchemaService(relationSchemaRepository, relationRegistry);
    }

    @Bean
    public RemEntityTypeResolver getRemEntityTypeResolver() {
        return new DefaultRemEntityTypeResolver();
    }

    @Bean
    public RemRegistry getRemRegistry() {
        return new InMemoryRemRegistry();
    }

    @Bean
    public Mode getLaunchMode() {
        return Optional.ofNullable(env.getProperty("mode")).map(mode -> Mode.valueOf(mode.toUpperCase())).orElse(Mode.SERVICE);
    }

    @Bean
    public QueryParametersBuilder getQueryParametersBuilder(QueryParser queryParser, AggregationParser aggregationParser,
            SortParser sortParser, PaginationParser paginationParser, SearchParser searchParser) {
        return new QueryParametersBuilder(queryParser, aggregationParser, sortParser, paginationParser, searchParser);
    }

    @Bean
    public AggregationParser getAggregationParser(CustomJsonParser customJsonParser) {
        return new JacksonAggregationParser(customJsonParser);
    }

    @Bean
    public QueryParser getQueryParser(CustomJsonParser customJsonParser) {
        return new JacksonQueryParser(customJsonParser);
    }

    @Bean
    public SortParser getSortParser(CustomJsonParser customJsonParser) {
        return new JacksonSortParser(customJsonParser);
    }

    @Bean
    public CustomJsonParser getCustomJsonParser(ObjectMapper objectMapper) {
        return new CustomJsonParser(objectMapper.getFactory());
    }

    @Bean
    public PaginationParser getPaginationParser() {
        return new DefaultPaginationParser();
    }

    @Bean
    public SearchParser getSearchParser(ObjectMapper objectMapper) {
        return new CustomSearchParser(objectMapper);
    }

    @Bean
    public LinksFilter getLinksFilter(LinkGenerator linkGenerator, RelationSchemaService relationSchemaService) {
        return new LinksFilter(linkGenerator, relationSchemaService);
    }

    @Bean
    public LinkGenerator getLinksGenerator() {
        return new DefaultLinkGenerator();
    }

    @Bean
    public CacheManager cacheManager(Environment env) {
        // Configure and return an implementation of Spring's CacheManager SPI
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        ConcurrentMapCache typeRelationsCache = new ConcurrentMapCache(RelationSchemaService.TYPE_RELATIONS_CACHE, CacheBuilder
                .newBuilder().expireAfterWrite(env.getProperty("resources.cache.typeRelations.expiry", Long.class), TimeUnit.MINUTES)
                .build().asMap(), false);
        ConcurrentMapCache relationFieldsCache = new ConcurrentMapCache(RelationSchemaService.RELATION_FIELDS_CACHE, CacheBuilder
                .newBuilder().expireAfterWrite(env.getProperty("resources.cache.relationFields.expiry", Long.class), TimeUnit.MINUTES)
                .build().asMap(), false);
        cacheManager.setCaches(Arrays.asList(typeRelationsCache, relationFieldsCache));
        return cacheManager;
    }

    @Bean
    public RelationRegistry relationRegistry() {
        return new InMemoryRelationRegistry();
    }

    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        return new InMemoryHealthCheckRegistry();
    }

    @Bean
    public PluginArtifactIdRegistry pluginInfoRegistry() {
        return new InMemoryPluginArtifactIdRegistry();
    }

    @Bean
    public PluginInfoResource pluginInfoResource(PluginArtifactIdRegistry pluginArtifactIdRegistry) {
        return new PluginInfoResource(pluginArtifactIdRegistry);
    }

    private int getPageSizeDefault() {
        return env.getProperty("api.defaultPageSize", Integer.class);
    }

    private int getMaxPageSizeDefault() {
        return env.getProperty("api.maxPageSize", Integer.class);
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected String getDatabaseName() {
        return "resources";
    }
}
