package com.bq.oss.corbel.resources.rem.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.resources.rem.dao.NamespaceNormalizer;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.CountResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Francisco Sanchez
 */
public class ElasticSearchResmiSearch implements ResmiSearch {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchResmiSearch.class);

    public static final String INDEX = "resmi";
    private static final String EMPTY_STRING = "";
    private final Client elasticsearchClient;
    private final Gson gson;
    private final NamespaceNormalizer namespaceNormalizer;

    public ElasticSearchResmiSearch(Client elasticsearchClient, NamespaceNormalizer namespaceNormalizer, Gson gson, String... stopWords) {
        createResourcesIndex(elasticsearchClient, INDEX, stopWords);
        this.elasticsearchClient = elasticsearchClient;
        this.namespaceNormalizer = namespaceNormalizer;
        this.gson = gson;
    }

    private void createResourcesIndex(Client elasticsearchClient, String index, String... languages) {
        if (!elasticsearchClient.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
            CreateIndexRequest indexRequest = new CreateIndexRequest(index);
            if (languages.length > 0) {
                indexRequest.settings(createStopWordsSettingsObject(languages));
            }
            elasticsearchClient.admin().indices().create(indexRequest).actionGet();
        }
    }

    @Override
    public JsonArray search(ResourceUri resourceUri, String search, String[] fields, int page, int size) {
        SearchResponse response = elasticsearchClient.prepareSearch(INDEX).setTypes(getElasticSearchType(resourceUri))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(QueryBuilders.multiMatchQuery(search, fields)).setFrom(page)
                .setSize(size).execute().actionGet();
        JsonArray jsonArray = new JsonArray();
        response.getHits().forEach(hit -> jsonArray.add(gson.toJsonTree(hit.getSource())));
        return jsonArray;
    }

    @Override
    public void indexDocument(ResourceUri resourceUri, JsonObject fields) {
        JsonPrimitive resourceId = resourceUri.isResource() ? new JsonPrimitive(resourceUri.getTypeId()) : new JsonPrimitive(
                resourceUri.getRelationId());
        fields.add("id", resourceId);
        UpdateRequest updateRequest = new UpdateRequest(INDEX, getElasticSearchType(resourceUri), getElasticSearchId(resourceUri))
                .doc(fields.toString());
        updateRequest.docAsUpsert(true);
        try {
            elasticsearchClient.update(updateRequest).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void deleteDocument(ResourceUri resourceUri) {
        elasticsearchClient.prepareDelete(INDEX, getElasticSearchType(resourceUri), getElasticSearchId(resourceUri)).execute().actionGet();
    }

    @Override
    public AggregationResult count(ResourceUri resourceUri, String search, String[] fields) {
        CountResponse response = elasticsearchClient.prepareCount(INDEX).setTypes(getElasticSearchType(resourceUri))
                .setQuery(QueryBuilders.multiMatchQuery(search, fields)).execute().actionGet();
        return new CountResult(response.getCount());
    }

    private String getElasticSearchType(ResourceUri resourceUri) {
        return Optional
                .ofNullable(namespaceNormalizer.normalize(resourceUri.getType()))
                .map(type -> type
                        + Optional.ofNullable(resourceUri.getRelation()).map(relation -> ":" + namespaceNormalizer.normalize(relation))
                                .orElse(EMPTY_STRING)).orElse(EMPTY_STRING);
    }

    private String getElasticSearchId(ResourceUri resourceUri) {
        return Optional
                .ofNullable(resourceUri.getTypeId())
                .map(type -> type
                        + Optional.ofNullable(resourceUri.getRelationId()).map(relationId -> ";r=" + relationId).orElse(EMPTY_STRING))
                .orElse(EMPTY_STRING);
    }

    private Map<String, String> createStopWordsSettingsObject(String... languages) {
        Map<String, String> settings = new HashMap<>();
        settings.put("analysis.filter.custom_stop_filter.type", "stop");
        int stopWordIndex = 0;
        for (String language : languages) {
            settings.put("analysis.filter.custom_stop_filter.stopwords." + stopWordIndex, "_" + language.trim() + "_");
            stopWordIndex++;
        }
        return settings;
    }
}
