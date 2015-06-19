package com.bq.oss.corbel.resources.rem.dao;

import java.util.List;
import java.util.Optional;

import com.bq.oss.corbel.resources.rem.model.GenericDocument;
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

    JsonObject findResource(ResourceUri uri);

    JsonArray findCollection(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination, Optional<Sort> sort);

    JsonElement findRelation(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination, Optional<Sort> sort);

    void updateResource(ResourceUri uri, JsonObject entity);

    boolean conditionalUpdateResource(ResourceUri uri, JsonObject jsonObject, List<ResourceQuery> resourceQueries);

    void saveResource(ResourceUri uri, Object entity);

    void createRelation(ResourceUri uri, JsonObject jsonObject) throws NotFoundException;

    JsonObject deleteResource(ResourceUri uri);

    List<GenericDocument> deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries);

    List<GenericDocument> deleteRelation(ResourceUri uri);

    CountResult count(ResourceUri resourceUri, List<ResourceQuery> resourceQueries);

    AverageResult average(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field);

    SumResult sum(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field);

    void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation);

    <T> List<T> findAll(String collection, Class<T> entityClass);

    void ensureExpireIndex(ResourceUri uri);

    void ensureIndex(ResourceUri uri, Index index);

}
