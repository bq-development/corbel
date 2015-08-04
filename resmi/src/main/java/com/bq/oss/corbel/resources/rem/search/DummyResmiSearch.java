package com.bq.oss.corbel.resources.rem.search;

import io.corbel.lib.queries.request.AggregationResult;
import io.corbel.lib.queries.request.CountResult;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
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
    public void indexDocument(ResourceUri resourceUri, JsonObject fields) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public void deleteDocument(ResourceUri resourceUri) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public AggregationResult count(ResourceUri resourceUri, String search) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
        return new CountResult();
    }

    @Override
    public void setupMapping(ResourceUri resourceUri, JsonObject mapping) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public void addTemplate(ResourceUri resourceUri, String name, JsonObject template) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public JsonArray search(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams, int page, int size) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
        return null;
    }

    @Override
    public AggregationResult count(ResourceUri resourceUri, String templateName, Map<String, Object> templateParams) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
        return null;
    }

    @Override
    public void addAlias(String alias) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public void removeAlias(String alias) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
    }

    @Override
    public JsonArray search(ResourceUri uri, String search, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination,
            Optional<Sort> sort) {
        LOG.debug(DISABLE_RESMI_SEARCH_MESSAGE);
        return null;
    }
}
