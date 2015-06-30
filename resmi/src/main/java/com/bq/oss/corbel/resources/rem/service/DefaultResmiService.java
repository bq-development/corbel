package com.bq.oss.corbel.resources.rem.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.core.index.Index;

import com.bq.oss.corbel.resources.rem.dao.NotFoundException;
import com.bq.oss.corbel.resources.rem.dao.RelationMoveOperation;
import com.bq.oss.corbel.resources.rem.dao.ResmiDao;
import com.bq.oss.corbel.resources.rem.model.GenericDocument;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchResource;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.CollectionParametersImpl;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.bq.oss.corbel.resources.rem.search.ResmiSearch;
import com.bq.oss.lib.queries.builder.ResourceQueryBuilder;
import com.bq.oss.lib.queries.request.Aggregation;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.Average;
import com.bq.oss.lib.queries.request.QueryOperator;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Search;
import com.bq.oss.lib.queries.request.Sum;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Francisco Sanchez
 */
public class DefaultResmiService implements ResmiService {

    private static final String _DST_ID = "_dst_id";
    private static final String _SRC_ID = "_src_id";
    private static final String _ORDER = "_order";
    private static final String _EXPIRE_AT = "_expireAt";
    private static final String _CREATED_AT = "_createdAt";
    private static final String _UPDATED_AT = "_updatedAt";
    private static final String _ACL = "_acl";
    private final static Set<String> ignorableReservedAttributeNames = Sets.newHashSet(_ID, _EXPIRE_AT, _ORDER, _SRC_ID, _DST_ID,
            _CREATED_AT, _UPDATED_AT, _ACL);

    public final static String SEARCHABLE_FIELDS = "searchable";

    private final ResmiDao resmiDao;
    private final ResmiSearch search;
    private final SearchableFieldsRegistry searchableFieldsRegistry;
    private final Clock clock;

    public DefaultResmiService(ResmiDao resmiDao, ResmiSearch search, SearchableFieldsRegistry searchableFieldsRegistry, Clock clock) {
        this.resmiDao = resmiDao;
        this.search = search;
        this.searchableFieldsRegistry = searchableFieldsRegistry;
        this.clock = clock;

        initSearchableFieldsRegistry();
    }

    private void initSearchableFieldsRegistry() {
        getSearchableFields().stream().forEach(searchableFieldsRegistry::addFields);
    }

    @Override
    public AggregationResult aggregate(ResourceUri resourceUri, CollectionParameters apiParameters) throws BadConfigurationException {
        Aggregation operation = apiParameters.getAggregation().get();
        switch (operation.getOperator()) {
            case $COUNT:
                if (!apiParameters.getSearch().isPresent()) {
                    return resmiDao.count(resourceUri, operation.operate(apiParameters.getQueries().orElse(null)));
                } else {
                    return countWithSearchService(resourceUri, apiParameters);
                }
            case $AVG:
                return resmiDao.average(resourceUri, operation.operate(apiParameters.getQueries().orElse(null)),
                        ((Average) operation).getField());
            case $SUM:
                return resmiDao.sum(resourceUri, operation.operate(apiParameters.getQueries().orElse(null)), ((Sum) operation).getField());
            default:
                throw new RuntimeException("Aggregation operation not supported: " + operation.getOperator());
        }
    }

    private AggregationResult countWithSearchService(ResourceUri resourceUri, CollectionParameters apiParameters)
            throws BadConfigurationException {
        Search searchObject = apiParameters.getSearch().get();
        return search.count(resourceUri, searchObject.getText(), getSearchableFields(resourceUri, searchObject));
    }

    @Override
    public JsonArray findCollection(ResourceUri uri, Optional<? extends CollectionParameters> apiParameters) throws BadConfigurationException {
        if (apiParameters.flatMap(params -> params.getSearch()).isPresent()) {
            return findInSearchService(uri, apiParameters.get());
        } else {
            return resmiDao.findCollection(uri,
                    apiParameters.flatMap(params -> params.getQueries()),
                    apiParameters.map(params -> params.getPagination()),
                    apiParameters.flatMap(params -> params.getSort()));
        }
    }

    private JsonArray findInSearchService(ResourceUri resourceUri, CollectionParameters apiParameters) throws BadConfigurationException {
        Search searchObject = apiParameters.getSearch().get();

        JsonArray searchResult = search.search(resourceUri, searchObject.getText(), getSearchableFields(resourceUri, searchObject),
                apiParameters.getPagination().getPage(), apiParameters.getPagination().getPageSize());

        if (searchObject.isBinded()) {
            CollectionParameters parameters = buildParametersForBinding(apiParameters, searchResult);
            return findCollection(resourceUri, Optional.of(parameters));
        } else {
            return searchResult;
        }
    }

    private String[] getSearchableFields(ResourceUri resourceUri, Search search) throws BadConfigurationException {
        Set<String> fields = searchableFieldsRegistry.getFieldsFromResourceUri(resourceUri);
        if (fields.isEmpty()) {
            throw new BadConfigurationException(String.format("Resource %1$s has no index defined", resourceUri.getType()));
        }

        if (search.getFields().isPresent() && !search.getFields().get().isEmpty()) {
            fields = Sets.intersection(search.getFields().get(), fields);
        }

        return fields.toArray(new String[fields.size()]);
    }

    private CollectionParameters buildParametersForBinding(CollectionParameters apiParameters, JsonArray searchResult) {
        List<String> ids = new ArrayList<>();
        for (JsonElement element : searchResult) {
            ids.add(((JsonObject) element).get("id").getAsString());
        }
        ResourceQueryBuilder builder = new ResourceQueryBuilder().add("id", ids, QueryOperator.$IN);
        return new CollectionParametersImpl(apiParameters.getPagination(), apiParameters.getSort(), Optional.of(Arrays.asList(builder
                .build())), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public JsonObject findResource(ResourceUri uri) {
        return resmiDao.findResource(uri);
    }

    @Override
    public JsonElement findRelation(ResourceUri uri, Optional<RelationParameters> apiParameters) throws BadConfigurationException {
        if (apiParameters.flatMap(params -> params.getSearch()).isPresent()) {
            return findInSearchService(uri, apiParameters.get());
        } else {
            return resmiDao.findRelation(uri, apiParameters.flatMap(params -> params.getQueries()),
                    apiParameters.map(params -> params.getPagination()),
                    apiParameters.flatMap(params -> params.getSort()));
        }
    }

    @Override
    public JsonObject saveResource(ResourceUri uri, JsonObject object, Optional<String> optionalUserId)
            throws StartsWithUnderscoreException {
        verifyNotUnderscore(object);
        optionalUserId.ifPresent(userId -> setId(userId, object));
        addDates(object);
        resmiDao.saveResource(uri, object);
        indexInSearchService(uri.setTypeId(object.get(ID).getAsString()), object);
        return object;
    }

    @Override
    public JsonObject updateResource(ResourceUri uri, JsonObject jsonObject) throws StartsWithUnderscoreException {
        verifyNotUnderscore(jsonObject);
        addDates(jsonObject);
        resmiDao.updateResource(uri, jsonObject);
        indexInSearchService(uri, jsonObject);
        return jsonObject;
    }

    @Override
    public JsonObject conditionalUpdateResource(ResourceUri uri, JsonObject jsonObject, List<ResourceQuery> resourceQueries)
            throws StartsWithUnderscoreException {
        verifyNotUnderscore(jsonObject);
        addDates(jsonObject);
        boolean found = resmiDao.conditionalUpdateResource(uri, jsonObject, resourceQueries);
        if (found) {
            indexInSearchService(uri, jsonObject);
            return jsonObject;
        }
        return null;
    }

    private void indexInSearchService(ResourceUri resourceUri, JsonObject jsonObject) {
        Set<String> fields = searchableFieldsRegistry.getFieldsFromResourceUri(resourceUri);
        if (!fields.isEmpty()) {
            search.indexDocument(resourceUri, pickJSonFields(jsonObject, fields));
        }
    }

    private JsonObject pickJSonFields(JsonObject jsonObject, Set<String> fields) {
        JsonObject searchableJsonObject = new JsonObject();
        fields.stream().filter(jsonObject::has).forEach(field -> searchableJsonObject.add(field, jsonObject.get(field)));
        return searchableJsonObject;
    }

    @Override
    public JsonObject createRelation(ResourceUri uri, JsonObject requestEntity) throws NotFoundException, StartsWithUnderscoreException {
        verifyNotUnderscore(requestEntity);
        addDates(requestEntity);
        resmiDao.createRelation(uri, requestEntity);
        indexInSearchService(uri, requestEntity);
        return requestEntity;
    }

    @Override
    public void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation) {
        if (uri.isTypeWildcard()) {
            throw new IllegalArgumentException("Relation origin must not be a wildcard");
        }
        resmiDao.moveRelation(uri, relationMoveOperation);
    }

    @Override
    public void deleteResource(ResourceUri uri) {
        resmiDao.deleteResource(uri);
        deleteInSearchService(uri);
    }

    private void deleteInSearchService(ResourceUri uri) {
        if (!searchableFieldsRegistry.getFieldsFromType(uri.getType()).isEmpty()) {
            search.deleteDocument(uri);
        }
    }

    @Override
    public void deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        List<GenericDocument> deleteEntries = resmiDao.deleteCollection(uri, queries);
        deleteInSearchService(uri, deleteEntries);
    }

    @Override
    public void deleteRelation(ResourceUri uri) {
        List<GenericDocument> deleteEntries = resmiDao.deleteRelation(uri);
        deleteInSearchService(uri, deleteEntries);
    }

    private void deleteInSearchService(ResourceUri uri, List<GenericDocument> deleteEntries) {
        if (!searchableFieldsRegistry.getFieldsFromType(uri.getType()).isEmpty()) {
            for (GenericDocument deleteEntry : deleteEntries) {
                search.deleteDocument(uri.setRelationId(deleteEntry.getId()));
            }
        }
    }

    @Override
    public void ensureExpireIndex(ResourceUri uri) {
        resmiDao.ensureExpireIndex(uri);
    }

    @Override
    public void ensureIndex(ResourceUri uri, Index index) {
        resmiDao.ensureIndex(uri, index);
    }

    @Override
    public void removeObjectId(JsonObject object) {
        if (object.has(ResmiService.ID)) {
            object.remove(ResmiService.ID);
        }
    }

    @Override
    public List<SearchResource> getSearchableFields() {
        return resmiDao.findAll(SEARCHABLE_FIELDS, SearchResource.class);
    }

    @Override
    public void addSearchableFields(SearchResource searchResource) {
        search.addResource(searchResource);
        resmiDao.saveResource(new ResourceUri(SEARCHABLE_FIELDS), searchResource);
        searchableFieldsRegistry.addFields(searchResource);
    }

    private void setId(String userId, JsonObject jsonObject) {
        String id = userId != null ? generateIdWithUserId(userId) : generateId();
        jsonObject.add(ResmiService.ID, new JsonPrimitive(id));
    }

    private String generateIdWithUserId(String userId) {
        return userId + ":" + generateId();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private void addDates(JsonObject entity) {
        if (entity == null) {
            return;
        }

        Date date = Date.from(clock.instant());
        String formatedDate = formatDate(date);

        JsonElement createdAt = entity.get(_CREATED_AT);
        if (createdAt == null) {
            entity.addProperty(_CREATED_AT, formatedDate);
        } else {
            entity.addProperty(_CREATED_AT, formatDate(Date.from(Instant.ofEpochMilli(createdAt.getAsLong()))));
        }

        entity.remove(_UPDATED_AT);
        entity.addProperty(_UPDATED_AT, formatedDate);
    }

    private String formatDate(Date date) {
        return "ISODate(" + String.format("%tFT%<tT.%<tLZ", date) + ")";
    }

    private JsonObject verifyNotUnderscore(JsonObject entity) throws StartsWithUnderscoreException {
        if (entity != null) {
            for (Map.Entry<String, JsonElement> entry : entity.entrySet()) {
                String key = entry.getKey();

                if (key.startsWith("_") && !ignorableReservedAttributeNames.contains(key)) {
                    throw new StartsWithUnderscoreException(entry.getKey());
                }
            }
        }
        return entity;
    }

}
