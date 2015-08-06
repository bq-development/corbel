package io.corbel.resources.rem.search;

import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.corbel.resources.rem.model.ResourceUri;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public interface ResmiSearch {

    JsonArray search(ResourceUri uri, String search, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination,
            Optional<Sort> sort);

    void indexDocument(ResourceUri uri, JsonObject fields);

    void deleteDocument(ResourceUri uri);

    AggregationResult count(ResourceUri uri, String search);

    JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, int page, int size);

    void setupMapping(ResourceUri resourceUri, JsonObject mapping);

    void addTemplate(ResourceUri resourceUri, String name, JsonObject template);

    AggregationResult count(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams);

    void addAlias(String alias);

    void removeAlias(String alias);
}
