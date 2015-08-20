package io.corbel.resources.rem.search;

import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.CountResult;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.dao.NamespaceNormalizer;
import io.corbel.resources.rem.model.ResourceUri;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Francisco Sanchez
 */
public class ElasticSearchResmiSearch implements ResmiSearch {

    public static final String INDEX = "resmi";
    private static final String EMPTY_STRING = "";
    private final ElasticSearchService elasticeSerachService;
    private final NamespaceNormalizer namespaceNormalizer;

    public ElasticSearchResmiSearch(ElasticSearchService elasticeSerachService, NamespaceNormalizer namespaceNormalizer) {
        this.elasticeSerachService = elasticeSerachService;
        this.namespaceNormalizer = namespaceNormalizer;
    }

    @Override
    public JsonArray search(ResourceUri uri, String templateName, Map<String, Object> templateParams, int page, int size) {
        return elasticeSerachService.search(INDEX, getElasticSearchType(uri), templateName, templateParams, page, size);
    }

    @Override
    public AggregationResult count(ResourceUri uri, String templateName, Map<String, Object> templateParams) {
        return elasticeSerachService.count(INDEX, getElasticSearchType(uri), templateName, templateParams);
    }

    @Override
    public JsonArray search(ResourceUri uri, String search, Optional<List<ResourceQuery>> queries, Pagination pagination,
            Optional<Sort> sort) {
        return elasticeSerachService.search(INDEX, getElasticSearchType(uri), search, queries.orElseGet(Collections::emptyList),
                pagination, sort);
    }

    @Override
    public AggregationResult count(ResourceUri uri, String search, Optional<List<ResourceQuery>> queries) {
        return new CountResult(elasticeSerachService.count(INDEX, getElasticSearchType(uri), search,
                queries.orElse(Collections.emptyList())));
    }

    @Override
    public void indexDocument(ResourceUri uri, JsonObject fields) {
        Optional.ofNullable(uri.isResource() ? uri.getTypeId() : uri.getRelationId()).map(JsonPrimitive::new).ifPresent(resourceId -> {
            fields.add("id", resourceId);
            elasticeSerachService.indexDocument(INDEX, getElasticSearchType(uri), getElasticSearchId(uri), fields.toString());
        });
    }

    @Override
    public void deleteDocument(ResourceUri uri) {
        elasticeSerachService.deleteDocument(INDEX, getElasticSearchType(uri), getElasticSearchId(uri));
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
