package com.bq.oss.corbel.resources.rem.search;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.resources.rem.dao.NamespaceNormalizer;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchResource;
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
    private final String mappingSettingsPath;

    public ElasticSearchResmiSearch(Client elasticsearchClient, String indexSettingsPath, String mappingSettingsPath, NamespaceNormalizer namespaceNormalizer, Gson gson) {
        createResourcesIndex(elasticsearchClient, INDEX, indexSettingsPath);
        this.elasticsearchClient = elasticsearchClient;
        this.namespaceNormalizer = namespaceNormalizer;
        this.mappingSettingsPath = mappingSettingsPath;
        this.gson = gson;
    }

    private void createResourcesIndex(Client elasticsearchClient, String index, String indexSettingsPath) {
        if (!elasticsearchClient.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
            CreateIndexRequest indexRequest = new CreateIndexRequest(index);
            try {
                indexRequest.settings(new String(Files.readAllBytes(Paths.get(getClass().getResource(indexSettingsPath).toURI()))));
            } catch (IOException | URISyntaxException e) {
                LOG.error("Unable to read elasticsearch index settings", e);
            }
            elasticsearchClient.admin().indices().create(indexRequest).actionGet();
        }
    }

    @Override
    public void addResource(SearchResource fields) {
        String type = fields.getResourceUri().getType();
        try {
            boolean mappingTypeNotCreated = false;
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = elasticsearchClient.admin().indices()
                    .prepareGetMappings(INDEX).execute().actionGet().mappings();
            if (!mappings.get(INDEX).containsKey(type)) {
                PutMappingRequest mappingRequest = new PutMappingRequest(INDEX).type(type);
                mappingRequest.source(new String(Files.readAllBytes(Paths.get(getClass().getResource(mappingSettingsPath)
                        .toURI()))));
                elasticsearchClient.admin().indices().putMapping(mappingRequest).actionGet();
                mappingTypeNotCreated = true;
            }
            for(String field : fields.getFields()) {
                if (mappingTypeNotCreated || !checkFieldCurrentlyMapped(mappings, type, field)) {
                    PutMappingRequest mappingRequest = new PutMappingRequest(INDEX).type(type);
                    mappingRequest.ignoreConflicts(true);
                    XContentBuilder properties = XContentFactory.jsonBuilder().startObject().startObject("properties");
                    properties.startObject(field);
                    properties.field("type", SearchResource.DEFAULT_FIELD_TYPE);
                    properties.field("index", "not_analyzed");
                    properties.endObject();
                    properties.endObject().endObject();
                    mappingRequest.source(properties);
                    elasticsearchClient.admin().indices().putMapping(mappingRequest).actionGet();
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOG.error("Unable to create mapping settings for " + type + " type", e);
        }
    }

    private boolean checkFieldCurrentlyMapped(ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings, String type,
            String field) throws IOException {
        return ((LinkedHashMap) mappings.get(INDEX).get(type).getSourceAsMap().get("properties")).containsKey(field);
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
}
