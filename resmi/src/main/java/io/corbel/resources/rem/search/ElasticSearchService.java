package io.corbel.resources.rem.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;

/**
 * Created by Francisco Sanchez on 18/12/15.
 */
public interface ElasticSearchService {
    boolean indexExists(String index);

    void createIndex(String index, String settings);

    void addAlias(String index, String alias);

    void removeAlias(String index, String alias);

    void setupMapping(String index, String type, String source);

    void addTemplate(String index, String source);

    JsonArray search(String index, String type, String search, List<ResourceQuery> queries, Pagination pagination, Optional<Sort> sort) throws InvalidApiParamException;

    JsonArray search(String index, String type, String templateName, Map<String, Object> templateParams, int page, int size) throws InvalidApiParamException;

    long count(String index, String type, String search, List<ResourceQuery> queries);

    long count(String index, String type, String templateName, Map<String, Object> templateParams) throws InvalidApiParamException;

    void indexDocument(String index, String type, String id, String source);

    void deleteDocument(String index, String type, String id);
}
