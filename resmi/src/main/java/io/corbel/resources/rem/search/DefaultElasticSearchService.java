package io.corbel.resources.rem.search;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;

import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Rubén Carrasco
 *
 */
public class DefaultElasticSearchService implements ElasticSearchService {
    private static final String MUSTACHE = "mustache";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultResmiSearch.class);

    private final Client client;
    private final Gson gson;

    public DefaultElasticSearchService(Client client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    @Override
    public boolean indexExists(String index) {
        client.admin().cluster().prepareHealth(index).setWaitForYellowStatus().execute();
        return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
    }

    @Override
    public void createIndex(String index, String settings) {
        client.admin().indices().prepareCreate(index).setSettings(settings).execute().actionGet();
        client.admin().cluster().prepareHealth(index).setWaitForYellowStatus().execute().actionGet();
    }


    @Override
    public void addAlias(String index, String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest().addAlias(alias, index);
        client.admin().indices().aliases(request).actionGet();
    }

    @Override
    public void removeAlias(String index, String alias) {
        IndicesAliasesRequest request = new IndicesAliasesRequest().removeAlias(alias, index);
        client.admin().indices().aliases(request).actionGet();
    }

    @Override
    public void setupMapping(String index, String type, String source) {
        if (indexExists(index)) {
            client.admin().indices().close(new CloseIndexRequest(index)).actionGet();
            PutMappingRequest mappingRequest = new PutMappingRequest(index).type(type).source(source).updateAllTypes(true);
            client.admin().indices().putMapping(mappingRequest).actionGet();
            client.admin().indices().open(new OpenIndexRequest(index)).actionGet();
        }
    }

    @Override
    public void addTemplate(String index, String source) {
        client.preparePutIndexedScript(MUSTACHE, index, source).get();
    }

    @Override
    public JsonArray search(String index, String type, String search, List<ResourceQuery> queries, Pagination pagination,
            Optional<Sort> sort) throws InvalidApiParamException {
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(ElasticSearchResourceQueryBuilder.build(search, queries)).setFrom(pagination.getPage() * pagination.getPageSize())
                .setSize(pagination.getPageSize());

        if (sort.isPresent()) {
            request.addSort(sort.get().getField(), SortOrder.valueOf(sort.get().getDirection().name()));
        }

        return extractResponse(executeAndGetSearchRequest(request));
    }

    @Override
    public JsonArray search(String index, String type, String templateName, Map<String, Object> templateParams, int page, int size) throws InvalidApiParamException {
        return extractResponse(search(index, type, templateName, templateParams, Optional.of(page), Optional.of(size)));
    }

    @Override
    public long count(String index, String type, String search, List<ResourceQuery> queries) {
        return client.prepareCount(index).setTypes(type).setQuery(ElasticSearchResourceQueryBuilder.build(search, queries)).execute()
                .actionGet().getCount();
    }

    @Override
    public long count(String index, String type, String templateName, Map<String, Object> templateParams) throws InvalidApiParamException {
        SearchResponse response = search(index, type, templateName, templateParams, Optional.empty(), Optional.empty());
        return response.getHits().getTotalHits();
    }

    private SearchResponse search(String index, String type, String templateName, Map<String, Object> templateParams,
            Optional<Integer> page, Optional<Integer> size) throws InvalidApiParamException {
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setTemplateName(templateName).setTemplateType(ScriptType.INDEXED).setTemplateParams(templateParams);
        if (page.isPresent() && size.isPresent()) {
            request.setFrom(page.get() * size.get()).setSize(size.get());
        }
        return executeAndGetSearchRequest(request);

    }

    private JsonArray extractResponse(SearchResponse response) {
        JsonArray jsonArray = new JsonArray();
        response.getHits().forEach(hit -> jsonArray.add(gson.toJsonTree(hit.getSource())));
        return jsonArray;
    }

    @Override
    public void indexDocument(String index, String type, String id, String source) {
        UpdateRequest updateRequest = new UpdateRequest(index, type, id).doc(source);
        updateRequest.docAsUpsert(true);
        try {
            client.update(updateRequest).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error indexing document", e);
        }
    }

    @Override
    public void deleteDocument(String index, String type, String id) {
        client.prepareDelete(index, type, id).execute().actionGet();
    }

    private SearchResponse executeAndGetSearchRequest(SearchRequestBuilder searchRequestBuilder) throws InvalidApiParamException {
        try {
            return searchRequestBuilder.execute().actionGet();
        } catch (SearchPhaseExecutionException sPEE) {
            if (sPEE.status().equals(RestStatus.BAD_REQUEST)) {
                throw new InvalidApiParamException(sPEE.getMessage());
            }
            throw sPEE;
        }
    }


}
