package com.bq.oss.corbel.resources.rem.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.CountResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public class DummyResmiSearch implements ResmiSearch {

    private static final String DISABLE_RESMI_SEARCH_MESSAGE = "api:search in RESMI is disable";

    private static final Logger LOG = LoggerFactory.getLogger(DummyResmiSearch.class);

    public DummyResmiSearch() {
        LOG.warn(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public JsonArray search(ResourceUri resourceUri, String search, String[] fields, int page, int size) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
        return new JsonArray();
    }

    @Override
    public void indexDocument(ResourceUri resourceUri, JsonObject fields) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public void deleteDocument(ResourceUri resourceUri) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public AggregationResult count(ResourceUri resourceUri, String search, String[] fields) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
        return new CountResult();
    }
}
