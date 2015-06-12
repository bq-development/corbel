package com.bq.oss.corbel.resources.rem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.index.Index;

import com.bq.oss.corbel.resources.rem.dao.NotFoundException;
import com.bq.oss.corbel.resources.rem.dao.RelationMoveOperation;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchableFields;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
public interface ResmiService {

    String ID = "id";
    String _ID = "_id";

    JsonArray find(String type, CollectionParameters apiParameters) throws BadConfigurationException;

    JsonObject findResourceById(String type, ResourceId id);

    JsonElement findRelation(String type, ResourceId id, String relation, RelationParameters apiParameters)
            throws BadConfigurationException;

    AggregationResult aggregate(ResourceUri resourceUri, CollectionParameters apiParameters);

    JsonObject save(String type, JsonObject object, Optional<String> userId) throws StartsWithUnderscoreException;

    JsonObject upsert(String type, String id, JsonObject jsonObject) throws StartsWithUnderscoreException;

    JsonObject conditionalUpdate(String type, String id, JsonObject object, List<ResourceQuery> resourceQueries)
            throws StartsWithUnderscoreException;

    JsonObject createRelation(String type, String id, String relation, String uri, JsonObject requestEntity) throws NotFoundException,
            StartsWithUnderscoreException;

    void moveElement(String type, ResourceId id, String relation, String uri, RelationMoveOperation relationMoveOperation);

    void deleteResourceById(String type, String id);

    void deleteRelation(String type, ResourceId id, String relation, Optional<String> dstId);

    List<SearchableFields> getSearchableFields();

    void addSearchableFields(SearchableFields searchableFields);

    void ensureExpireIndex(String type);

    void ensureCollectionIndex(String type, Index index);

    void ensureRelationIndex(String type, String relation, Index index);

    void removeObjectId(JsonObject object);
}
