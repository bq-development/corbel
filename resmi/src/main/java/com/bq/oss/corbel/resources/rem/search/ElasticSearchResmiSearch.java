package com.bq.oss.corbel.resources.rem.search;

import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.CountResult;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.resources.rem.dao.NamespaceNormalizer;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Francisco Sanchez
 */
public class ElasticSearchResmiSearch implements ResmiSearch {

    private static final String MUSTACHE = "mustache";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchResmiSearch.class);

    public static final String INDEX = "resmi";
    private static final String EMPTY_STRING = "";
    private final Client elasticsearchClient;
    private final Gson gson;
    private final NamespaceNormalizer namespaceNormalizer;

    public ElasticSearchResmiSearch(Client elasticsearchClient, String indexSettingsPath, String mappingSettingsPath,
            NamespaceNormalizer namespaceNormalizer, Gson gson) {
        this.elasticsearchClient = elasticsearchClient;
        this.namespaceNormalizer = namespaceNormalizer;
        this.gson = gson;
        createResourcesIndex(indexSettingsPath);
    }

    @SuppressWarnings("resource")
    private void createResourcesIndex(String indexSettingsPath) {
        if (!elasticsearchClient.admin().indices().prepareExists(INDEX).execute().actionGet().isExists()) {
            CreateIndexRequest indexRequest = new CreateIndexRequest(INDEX).settings(new Scanner(getClass().getResourceAsStream(
                    indexSettingsPath), "UTF-8").useDelimiter("\\A").next());
            elasticsearchClient.admin().indices().create(indexRequest).actionGet();
        }
    }

    @Override
    public void addAlias(String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest().addAlias(alias, INDEX);
        elasticsearchClient.admin().indices().aliases(request).actionGet();
    }

    @Override
    public void removeAlias(String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest().removeAlias(alias, INDEX);
        elasticsearchClient.admin().indices().aliases(request).actionGet();
    }

    @Override
    public void setupMapping(ResourceUri resourceUri, JsonObject mapping) {
        elasticsearchClient.admin().indices().close(new CloseIndexRequest(INDEX)).actionGet();
        PutMappingRequest mappingRequest = new PutMappingRequest(INDEX).type(getElasticSearchType(resourceUri)).source(mapping.toString())
                .ignoreConflicts(true);
        elasticsearchClient.admin().indices().putMapping(mappingRequest).actionGet();
        elasticsearchClient.admin().indices().open(new OpenIndexRequest(INDEX)).actionGet();
    }

    @Override
    public void addTemplate(ResourceUri resourceUri, String name, JsonObject template) {
        elasticsearchClient.preparePutIndexedScript(MUSTACHE, getElasticSearchType(resourceUri) + ":" + name, template.toString()).get();
    }

    @Override
    public JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, int page, int size) {
        return search(resourceUri, templateName, templateParams, Optional.of(page), Optional.of(size));
    }

    @Override
    public AggregationResult count(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams) {
        JsonArray jsonArray = search(resourceUri, templateName, templateParams, Optional.empty(), Optional.empty());
        return new CountResult(jsonArray.size());
    }

    private JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, Optional<Integer> page,
            Optional<Integer> size) {
        SearchRequestBuilder requestBuilder = elasticsearchClient.prepareSearch(INDEX).setTypes(getElasticSearchType(resourceUri))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setTemplateName(templateName).setTemplateType(ScriptType.INDEXED)
                .setTemplateParams(templateParams);
        if (page.isPresent() && size.isPresent()) {
            requestBuilder.setFrom(page.get()).setSize(size.get());
        }
        JsonArray jsonArray = new JsonArray();
        requestBuilder.execute().actionGet().getHits().forEach(hit -> jsonArray.add(gson.toJsonTree(hit.getSource())));
        return jsonArray;
    }

    @Override
    public JsonArray search(ResourceUri uri, String search, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination,
            Optional<Sort> sort) {

        SearchRequestBuilder request = elasticsearchClient.prepareSearch(INDEX).setTypes(getElasticSearchType(uri))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(ElasticSearchResourceQueryBuilder.build(search, resourceQueries.orElse(Collections.emptyList())))
                .setFrom(pagination.getPage()).setSize(pagination.getPageSize());

        if (sort.isPresent()) {
            request.addSort(sort.get().getField(), SortOrder.valueOf(sort.get().getDirection().name()));
        }

        JsonArray jsonArray = new JsonArray();
        request.execute().actionGet().getHits().forEach(hit -> jsonArray.add(gson.toJsonTree(hit.getSource())));
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
    public AggregationResult count(ResourceUri resourceUri, String search) {
        CountResponse response = elasticsearchClient.prepareCount(INDEX).setTypes(getElasticSearchType(resourceUri))
                .setQuery(QueryBuilders.queryStringQuery(search)).execute().actionGet();
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
