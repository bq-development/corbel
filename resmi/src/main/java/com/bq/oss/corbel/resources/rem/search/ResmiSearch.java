package com.bq.oss.corbel.resources.rem.search;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public interface ResmiSearch {

    JsonArray search(ResourceUri uri, String search, String[] fields, int page, int size);

    void indexDocument(ResourceUri uri, JsonObject fields);

    void deleteDocument(ResourceUri uri);

    AggregationResult count(ResourceUri uri, String search, String[] fields);
}
