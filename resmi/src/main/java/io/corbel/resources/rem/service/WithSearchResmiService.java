package io.corbel.resources.rem.service;

import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.model.GenericDocument;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.model.SearchResource;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.CollectionParametersImpl;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RelationParametersImpl;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import io.corbel.resources.rem.search.ResmiSearch;

import java.time.Clock;
import java.util.*;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public class WithSearchResmiService extends DefaultResmiService implements SearchableFieldsService {

    private static final String ALL_FIELDS = "*";
    public final static String SEARCHABLE_FIELDS = "searchable";
    private static final String RESMI_DOMAIN = "_resmi";
    private static final String DST_ID = "_dst_id";

    private final ResmiSearch search;
    private final SearchableFieldsRegistry searchableFieldsRegistry;
    private final Gson gson;

    public WithSearchResmiService(ResmiDao resmiDao, ResmiSearch search, SearchableFieldsRegistry searchableFieldsRegistry, Gson gson,
            Clock clock) {
        super(resmiDao, clock);
        this.search = search;
        this.searchableFieldsRegistry = searchableFieldsRegistry;
        this.gson = gson;
        initSearchableFieldsRegistry();
    }

    private void initSearchableFieldsRegistry() {
        getSearchableFields().stream().forEach(searchableFieldsRegistry::addFields);
    }

    @Override
    public JsonElement aggregate(ResourceUri resourceUri, CollectionParameters apiParameters) throws BadConfigurationException,
            InvalidApiParamException {
        Aggregation operation = apiParameters.getAggregation().get();
        switch (operation.getOperator()) {
            case $COUNT:
                if (apiParameters.getSearch().isPresent()) {
                    return gson.toJsonTree(countWithSearchService(resourceUri, apiParameters));
                }
            case $AVG:
            case $SUM:
            default:
                return super.aggregate(resourceUri, apiParameters);
        }
    }

    private JsonElement countWithSearchService(ResourceUri resourceUri, CollectionParameters apiParameters)
            throws BadConfigurationException, InvalidApiParamException {
        Search searchObject = apiParameters.getSearch().get();
        if (searchObject.getText().isPresent()) {
            List<ResourceQuery> queries = apiParameters.getQueries().orElseGet(ArrayList::new);
            addRelationQuery(resourceUri, queries);
            return search.count(resourceUri, searchObject.getText().get(), queries);
        } else {
            return search.count(resourceUri, searchObject.getTemplate().get(), searchObject.getParams().get());
        }
    }

    private void addRelationQuery(ResourceUri resourceUri, List<ResourceQuery> queries) {
        if (resourceUri.isRelation() && !resourceUri.isRelationWildcard()) {
            if (queries.isEmpty()) {
                queries.add(new ResourceQueryBuilder().build());
            }
            for (ResourceQuery resourceQuery : queries) {
                QueryNode queryNode = new QueryNodeImpl(QueryOperator.$EQ, _SRC_ID, new StringQueryLiteral(resourceUri.getTypeId()));
                resourceQuery.addQueryNode(queryNode);
            }
        }
    }

    @Override
    public JsonArray findCollection(ResourceUri uri, Optional<CollectionParameters> apiParameters) throws BadConfigurationException, InvalidApiParamException {
        if (apiParameters.flatMap(CollectionParameters::getSearch).isPresent()) {
            return findInSearchService(uri, apiParameters.get());
        } else {
            return super.findCollection(uri, apiParameters);
        }
    }

    private JsonArray findInSearchService(ResourceUri resourceUri, CollectionParameters apiParameters) throws BadConfigurationException, InvalidApiParamException {
        Search searchObject = apiParameters.getSearch().get();
        JsonArray searchResult;
        if (searchObject.getText().isPresent()) {
            List<ResourceQuery> queries = apiParameters.getQueries().orElseGet(ArrayList::new);
            addRelationQuery(resourceUri, queries);
            searchResult = search.search(resourceUri, searchObject.getText().get(), queries, apiParameters.getPagination(),
                    apiParameters.getSort());
        } else {
            searchResult = search.search(resourceUri, searchObject.getTemplate().get(), searchObject.getParams().get(), apiParameters
                    .getPagination().getPage(), apiParameters.getPagination().getPageSize());
        }

        if (searchObject.indexFieldsOnly()) {
            return searchResult;
        } else {
            JsonArray fullResult;
            if (resourceUri.isRelation()) {
                RelationParameters parameters = buildRelationParametersForBinding(apiParameters, searchResult);
                fullResult = (JsonArray) findRelation(resourceUri, Optional.of(parameters));
            } else {
                CollectionParameters parameters = buildCollectionParametersForBinding(apiParameters, searchResult);
                fullResult = findCollection(resourceUri, Optional.of(parameters));
            }
            if (!apiParameters.getSort().isPresent()) {
                return orderResult(searchResult, fullResult);
            } else {
                return fullResult;
            }
        }
    }

    private RelationParameters buildRelationParametersForBinding(CollectionParameters apiParameters, JsonArray searchResult) {
        List<StringQueryLiteral> ids = new ArrayList<>();
        for (JsonElement element : searchResult) {
            String id = ((JsonObject) element).get(ID).getAsString();
            ids.add(new StringQueryLiteral(id));
        }
        ResourceQueryBuilder builder = new ResourceQueryBuilder().add(DST_ID, ids, QueryOperator.$IN);
        Pagination pagination = apiParameters.getPagination();
        pagination.setPage(0);
        return new RelationParametersImpl(apiParameters.getPagination(), apiParameters.getSort(), Optional.of(Collections
                .singletonList(builder.build())), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }


    private CollectionParameters buildCollectionParametersForBinding(CollectionParameters apiParameters, JsonArray searchResult) {
        List<StringQueryLiteral> ids = new ArrayList<>();
        for (JsonElement element : searchResult) {
            String id = ((JsonObject) element).get(ID).getAsString();
            ids.add(new StringQueryLiteral(id));
        }
        ResourceQueryBuilder builder = new ResourceQueryBuilder().add(ID, ids, QueryOperator.$IN);
        Pagination pagination = apiParameters.getPagination();
        pagination.setPage(0);
        return new CollectionParametersImpl(pagination, apiParameters.getSort(), Optional.of(Collections.singletonList(builder.build())),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    private JsonArray orderResult(JsonArray searchResult, JsonArray fullResult) {
        JsonArray orderedArray = new JsonArray();

        Map<String, Integer> orderedMap = new HashMap<String, Integer>();
        StreamSupport.stream(searchResult.spliterator(), false).map(JsonElement::getAsJsonObject)
                .forEachOrdered(element -> orderedMap.put(element.get(ID).getAsString(), orderedMap.size()));
        StreamSupport
                .stream(fullResult.spliterator(), false)
                .sorted((jsonElement1, jsonElement2) -> Integer.compare(
                        orderedMap.get(jsonElement1.getAsJsonObject().get(ID).getAsString()),
                        orderedMap.get(jsonElement2.getAsJsonObject().get(ID).getAsString())))
                .forEach(element -> orderedArray.add(element));
        return orderedArray;
    }

    @Override
    public JsonElement findRelation(ResourceUri uri, Optional<RelationParameters> apiParameters) throws BadConfigurationException, InvalidApiParamException {
        if (apiParameters.flatMap(RelationParameters::getSearch).isPresent()) {
            return findInSearchService(uri, apiParameters.get());
        } else {
            return super.findRelation(uri, apiParameters);
        }
    }

    @Override
    public JsonObject saveResource(ResourceUri uri, JsonObject object, Optional<String> optionalUserId)
            throws StartsWithUnderscoreException {
        object = super.saveResource(uri, object, optionalUserId);
        indexInSearchService(uri.setTypeId(object.get(ID).getAsString()), object);
        return object;
    }

    @Override
    public JsonObject updateResource(ResourceUri uri, JsonObject jsonObject) throws StartsWithUnderscoreException {
        jsonObject = super.updateResource(uri, jsonObject);
        indexInSearchService(uri, jsonObject);
        return jsonObject;
    }

    @Override
    public JsonObject conditionalUpdateResource(ResourceUri uri, JsonObject jsonObject, List<ResourceQuery> resourceQueries)
            throws StartsWithUnderscoreException {
        jsonObject = super.conditionalUpdateResource(uri, jsonObject, resourceQueries);
        if (jsonObject != null) {
            indexInSearchService(uri, jsonObject);
            return jsonObject;
        }
        return null;
    }

    private void indexInSearchService(ResourceUri resourceUri, JsonObject jsonObject) {
        Set<String> fields = searchableFieldsRegistry.getFieldsFromResourceUri(resourceUri);
        if (!fields.isEmpty()) {
            jsonObject = pickJSonFields(jsonObject, fields);
            search.indexDocument(resourceUri, jsonObject);
        }
    }

    private JsonObject pickJSonFields(JsonObject jsonObject, Set<String> fields) {
        if (fields.contains(ALL_FIELDS)) {
            return jsonObject;
        }
        fields.add(_SRC_ID);
        JsonObject searchableJsonObject = new JsonObject();
        fields.stream().filter(jsonObject::has).forEach(field -> searchableJsonObject.add(field, jsonObject.get(field)));
        return searchableJsonObject;
    }

    @Override
    public JsonObject createRelation(ResourceUri uri, JsonObject requestEntity) throws NotFoundException, StartsWithUnderscoreException {
        requestEntity = super.createRelation(uri, requestEntity);
        requestEntity.addProperty(_SRC_ID, uri.getTypeId());
        indexInSearchService(uri, requestEntity);
        return requestEntity;
    }

    @Override
    public void deleteResource(ResourceUri uri) {
        super.deleteResource(uri);
        deleteInSearchService(uri);
    }

    private void deleteInSearchService(ResourceUri uri) {
        if (!searchableFieldsRegistry.getFieldsFromResourceUri(uri).isEmpty()) {
            search.deleteDocument(uri);
        }
    }

    @Override
    public void deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        List<GenericDocument> deleteEntries = resmiDao.deleteCollection(uri, queries);
        deleteInSearchService(uri, deleteEntries);
    }

    @Override
    public void deleteRelation(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        List<GenericDocument> deleteEntries = resmiDao.deleteRelation(uri, queries);
        deleteInSearchService(uri, deleteEntries);
    }

    private void deleteInSearchService(ResourceUri uri, List<GenericDocument> deleteEntries) {
        if (!searchableFieldsRegistry.getFieldsFromResourceUri(uri).isEmpty()) {
            for (GenericDocument deleteEntry : deleteEntries) {
                search.deleteDocument(uri.setRelationId(deleteEntry.getId()));
            }
        }
    }

    @Override
    public List<SearchResource> getSearchableFields() {
        return resmiDao.findAll(new ResourceUri(RESMI_DOMAIN, SEARCHABLE_FIELDS), SearchResource.class);
    }

    @Override
    public void addSearchableFields(SearchResource searchResource) {
        resmiDao.saveResource(new ResourceUri(RESMI_DOMAIN, SEARCHABLE_FIELDS), searchResource);
        searchableFieldsRegistry.addFields(searchResource);
    }

}
