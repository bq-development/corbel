package io.corbel.resources.rem.search;

import com.google.gson.JsonElement;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.model.ResourceUri;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public interface ResmiSearch {

    JsonArray search(ResourceUri uri, String search, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination,
            Optional<Sort> sort);

    JsonElement count(ResourceUri uri, String search, Optional<List<ResourceQuery>> resourceQueries);

    JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, int page, int size);

    JsonElement count(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams);

    void indexDocument(ResourceUri uri, JsonObject fields);

    void deleteDocument(ResourceUri uri);

    boolean upsertResmiIndex(String name);

    boolean upsertResmiIndex(String name, String settings);
}
