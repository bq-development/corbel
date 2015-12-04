package io.corbel.resources.rem.search;

import com.google.gson.JsonElement;
import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.dao.NamespaceNormalizer;
import io.corbel.resources.rem.model.ResourceUri;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Francisco Sanchez
 */
public class ElasticSearchResmiSearch implements ResmiSearch {

    public static final String RESMI_INDEX_PREFIX = "resmi_";
    private static final String EMPTY_STRING = "";
    private final ElasticSearchService elasticSearchService;
    private final NamespaceNormalizer namespaceNormalizer;
    private final String indexSettingsPath;
    private final Clock clock;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    public ElasticSearchResmiSearch(ElasticSearchService elasticeSerachService, NamespaceNormalizer namespaceNormalizer,
            String indexSettingsPath, AggregationResultsFactory aggregationResultsFactory, Clock clock) {
        this.elasticSearchService = elasticeSerachService;
        this.namespaceNormalizer = namespaceNormalizer;
        this.clock = clock;
        this.indexSettingsPath = indexSettingsPath;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @Override
    public boolean upsertResmiIndex(String name) {
        return upsertResmiIndex(name, indexSettingsPath);
    }

    @Override
    public boolean upsertResmiIndex(String name, String settings) {
        String finalAliasName = RESMI_INDEX_PREFIX + name;
        if (!elasticSearchService.indexExists(finalAliasName)) {
            String indexName = RESMI_INDEX_PREFIX + (name + clock.millis()).toLowerCase();
            elasticSearchService.createIndex(indexName, new Scanner(getClass().getResourceAsStream(settings), "UTF-8").useDelimiter("\\A")
                    .next());
            elasticSearchService.addAlias(indexName, finalAliasName);
            return false;
        }
        return true;
    }

    @Override
    public JsonArray search(ResourceUri uri, String templateName, Map<String, Object> templateParams, int page, int size) {
        String elasticSearchType = getElasticSearchType(uri);
        if (upsertResmiIndex(elasticSearchType)) {
            return elasticSearchService.search(RESMI_INDEX_PREFIX + elasticSearchType, elasticSearchType, templateName, templateParams,
                    page, size);
        } else {
            return new JsonArray();
        }
    }

    @Override
    public JsonElement count(ResourceUri uri, String templateName, Map<String, Object> templateParams) {
        String elasticSearchType = getElasticSearchType(uri);
        if (upsertResmiIndex(elasticSearchType)) {
            return aggregationResultsFactory.countResult(elasticSearchService.count(RESMI_INDEX_PREFIX + elasticSearchType, elasticSearchType, templateName, templateParams));
        } else {
           return aggregationResultsFactory.countResult(0);
        }
    }

    @Override
    public JsonArray search(ResourceUri uri, String search, Optional<List<ResourceQuery>> queries, Pagination pagination,
            Optional<Sort> sort) {
        String elasticSearchType = getElasticSearchType(uri);
        if (upsertResmiIndex(elasticSearchType)) {
            return elasticSearchService.search(RESMI_INDEX_PREFIX + elasticSearchType, elasticSearchType, search,
                    queries.orElseGet(Collections::emptyList), pagination, sort);
        } else {
            return new JsonArray();
        }
    }

    @Override
    public JsonElement count(ResourceUri uri, String search, Optional<List<ResourceQuery>> queries) {
        String elasticSearchType = getElasticSearchType(uri);
        if (upsertResmiIndex(elasticSearchType)) {
            return aggregationResultsFactory.countResult(elasticSearchService.count(RESMI_INDEX_PREFIX + elasticSearchType, elasticSearchType, search,
                    queries.orElse(Collections.emptyList())));
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
                            String elasticSearchType = getElasticSearchType(uri);
                            upsertResmiIndex(elasticSearchType);
                            elasticSearchService.indexDocument(RESMI_INDEX_PREFIX + elasticSearchType, elasticSearchType,
                                    getElasticSearchId(uri), fields.toString());
                        });
    }

    @Override
    public void deleteDocument(ResourceUri uri) {
        String elasticSearchType = getElasticSearchType(uri);
        if (upsertResmiIndex(elasticSearchType)) {
            elasticSearchService.deleteDocument(RESMI_INDEX_PREFIX + elasticSearchType, elasticSearchType, getElasticSearchId(uri));
        }
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
