package com.bq.oss.corbel.resources.rem.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.index.Index;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.lib.queries.request.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sánchez - Rubén Carrasco
 */
public interface ResmiDao {

    boolean exists(String type, String id);

    JsonObject findById(String type, String id);

    JsonArray find(String type, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination, Optional<Sort> sort);

    JsonElement findRelation(String type, ResourceId id, String relation, Optional<List<ResourceQuery>> resourceQueries,
            Pagination pagination, Optional<Sort> sort, Optional<String> dstId);

    void upsert(String type, String id, JsonObject entity);

    boolean findAndModify(String type, String id, JsonObject jsonObject, List<ResourceQuery> resourceQueries);

    void save(String type, Object entity);

    void createRelation(String type, String id, String relation, String uri, JsonObject jsonObject) throws NotFoundException;

    void deleteById(String type, String id);

    void deleteRelation(String type, ResourceId id, String relation, Optional<String> dstId);

    CountResult count(ResourceUri resourceUri, List<ResourceQuery> resourceQueries);

    AverageResult average(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field);

    SumResult sum(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field);

    void moveElement(String type, String id, String relation, String uri, RelationMoveOperation relationMoveOperation);

    <T> List<T> findAll(String collection, Class<T> entityClass);

    void ensureExpireIndex(String type);

    void ensureCollectionIndex(String type, Index index);

    void ensureRelationIndex(String type, String relation, Index index);

}
