package io.corbel.resources.rem.search;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.dao.NamespaceNormalizer;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;

/**
 * @author Francisco Sanchez
 */
public class DefaultResmiSearch implements ResmiSearch {
    private static final String RESMI_INDEX_PREFIX = "resmi_";
    private static final String DOMAIN_CONCATENATION = "__";
    private static final String EMPTY_STRING = "";
    private static final String ELASTICSEARCH_DEFAULT_MAPPING = "_default_";
    private final NamespaceNormalizer namespaceNormalizer;
    private final ElasticSearchService elasticSearchService;
    private final String indexSettingsPath;
    private final Clock clock;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;
    private final String defaultMappingSettingsPath;

    @SuppressWarnings("rawtypes")
    public DefaultResmiSearch(ElasticSearchService elasticeSerachService, NamespaceNormalizer namespaceNormalizer,
                              String indexSettingsPath, AggregationResultsFactory aggregationResultsFactory, Clock clock, String defaultMappingSettingsPath) {
        this.elasticSearchService = elasticeSerachService;
        this.clock = clock;
        this.indexSettingsPath = indexSettingsPath;
        this.aggregationResultsFactory = aggregationResultsFactory;
        this.defaultMappingSettingsPath = defaultMappingSettingsPath;
        this.namespaceNormalizer = namespaceNormalizer;
    }

    @Override
    public boolean upsertResmiIndex(ResourceUri uri) {
        return upsertResmiIndex(uri, indexSettingsPath, defaultMappingSettingsPath);
    }

    @Override
    public boolean upsertResmiIndex(ResourceUri uri, String settings, String defaultMapping) {
        String finalAliasName = getIndexName(uri);
        if (!elasticSearchService.indexExists(finalAliasName)) {
            String indexName = RESMI_INDEX_PREFIX + (getElasticSearchName(uri) + clock.millis()).toLowerCase();
            elasticSearchService.createIndex(indexName, new Scanner(getClass().getResourceAsStream(settings), "UTF-8").useDelimiter("\\A")
                    .next());
            elasticSearchService.addAlias(indexName, finalAliasName);
            elasticSearchService.setupMapping(indexName, ELASTICSEARCH_DEFAULT_MAPPING,
                    new Scanner(getClass().getResourceAsStream(defaultMapping), "UTF-8").useDelimiter("\\A").next());
            return false;
        }
        return true;
    }

    @Override
    public void setupMapping(ResourceUri uri, String mapping) {
        elasticSearchService.setupMapping(getIndexName(uri), getElasticSearchType(uri), mapping);
    }

    @Override
    public void createIndex(String domain, String name, String settings) {
        elasticSearchService.createIndex(getIndexName(new ResourceUri(domain, name)), settings);
    }

    @Override
    public JsonArray search(ResourceUri uri, String templateName, Map<String, Object> templateParams, int page, int size) throws InvalidApiParamException {
        if (upsertResmiIndex(uri)) {
            return elasticSearchService.search(getIndexName(uri), getElasticSearchType(uri), templateName, templateParams, page, size);
        } else {
            return new JsonArray();
        }
    }

    @Override
    public JsonArray search(ResourceUri uri, String search, List<ResourceQuery> queries, Pagination pagination, Optional<Sort> sort) throws InvalidApiParamException {
        if (upsertResmiIndex(uri)) {
            return elasticSearchService
                    .search(getIndexName(uri), getElasticSearchType(uri), search, queries, pagination, sort);
        } else {
            return new JsonArray();
        }
    }

    @Override
    public JsonElement count(ResourceUri uri, String templateName, Map<String, Object> templateParams) throws InvalidApiParamException {
        if (upsertResmiIndex(uri)) {
            return aggregationResultsFactory.countResult(elasticSearchService.count(getIndexName(uri), getElasticSearchType(uri), templateName, templateParams));
        } else {
            return aggregationResultsFactory.countResult(0);
        }
    }

    @Override
    public JsonElement count(ResourceUri uri, String search, List<ResourceQuery> queries) {
        if (upsertResmiIndex(uri)) {
            return aggregationResultsFactory.countResult(elasticSearchService.count(getIndexName(uri), getElasticSearchType(uri), search, queries));
        } else {
            return aggregationResultsFactory.countResult(0);
        }
    }

    @Override
    public void indexDocument(ResourceUri uri, JsonObject fields) {
        Optional.ofNullable(uri.isResource() ? uri.getTypeId() : uri.getRelationId())
                .map(JsonPrimitive::new)
                .ifPresent(
                        resourceId -> {
                            fields.add("id", resourceId);
                            upsertResmiIndex(uri);
                            elasticSearchService.indexDocument(getIndexName(uri), getElasticSearchType(uri),
                                    getElasticSearchId(uri), fields.toString());
                        });
    }

    @Override
    public void deleteDocument(ResourceUri uri) {
        if (upsertResmiIndex(uri)) {
            elasticSearchService.deleteDocument(getIndexName(uri),getElasticSearchType(uri), getElasticSearchId(uri));
        }
    }

    private String getIndexName(ResourceUri uri) {
        return RESMI_INDEX_PREFIX + getElasticSearchName(uri);
    }

    private String getElasticSearchName(ResourceUri uri) {
        return namespaceNormalizer.normalize(uri.getDomain()) + DOMAIN_CONCATENATION + getElasticSearchType(uri);
    }

    private String getElasticSearchType(ResourceUri uri) {
        return Optional
                .ofNullable(namespaceNormalizer.normalize(uri.getType()))
                .map(type -> type
                        + Optional.ofNullable(uri.getRelation()).map(relation -> ":" + namespaceNormalizer.normalize(relation))
                        .orElse(EMPTY_STRING)).orElse(EMPTY_STRING);
    }

    private String getElasticSearchId(ResourceUri uri) {
        return Optional.ofNullable(uri.getRelationId()).orElse(Optional.ofNullable(uri.getTypeId()).orElse(EMPTY_STRING));
    }
}
