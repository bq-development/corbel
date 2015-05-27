package com.bq.oss.corbel.resources.rem.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.utils.JsonUtils;
import com.bq.oss.lib.mongo.JsonObjectMongoWriteConverter;
import com.bq.oss.lib.mongo.utils.GsonUtil;
import com.bq.oss.lib.queries.request.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Alberto J. Rubio
 *
 */
public class MongoResmiDao implements ResmiDao {

    private static final String ID = "id";
    private static final String _ID = "_id";

    private static final String RELATION_CONCATENATOR = ".";
    private static final Logger LOG = LoggerFactory.getLogger(MongoResmiDao.class);
    private static final String EMPTY_STRING = "";
    private static final String EXPIRE_AT = "_expireAt";
    private static final String CREATED_AT = "_createdAt";

    private final MongoOperations mongoOperations;
    private final JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter;
    private final NamespaceNormalizer namespaceNormalizer;
    private final ResmiOrder resmiOrder;

    public MongoResmiDao(MongoOperations mongoOperations, JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter,
            NamespaceNormalizer namespaceNormalizer, ResmiOrder resmiOrder) {
        this.mongoOperations = mongoOperations;
        this.jsonObjectMongoWriteConverter = jsonObjectMongoWriteConverter;
        this.namespaceNormalizer = namespaceNormalizer;
        this.resmiOrder = resmiOrder;
    }

    @Override
    public boolean exists(String type, String id) {
        return mongoOperations.exists(Query.query(Criteria.where(_ID).is(id)), namespaceNormalizer.normalize(type));
    }

    @Override
    public JsonObject findById(String type, String id) {
        return mongoOperations.findById(id, JsonObject.class, namespaceNormalizer.normalize(type));
    }

    @Override
    public JsonArray find(String type, Optional<List<ResourceQuery>> resourceQueries, Pagination pagination, Optional<Sort> sort) {
        Query query = new MongoResmiQueryBuilder().query(resourceQueries.orElse(null)).pagination(pagination).sort(sort.orElse(null))
                .build();
        LOG.debug("Query executed : " + query.getQueryObject().toString());
        return find(namespaceNormalizer.normalize(type), query);
    }

    @Override
    public JsonElement findRelation(String type, ResourceId id, String relationType, Optional<List<ResourceQuery>> resourceQueries,
            Pagination pagination, Optional<Sort> sort, Optional<String> dstId) {
        if (dstId.isPresent()) {
            Query query = new MongoResmiQueryBuilder().relationDestinationId(dstId.get()).build();
            query.fields().exclude(_ID).exclude(JsonRelation._SRC_ID);
            return renameIds(findRelation(namespaceNormalizer.normalize(type), id.getId(), namespaceNormalizer.normalize(relationType),
                    dstId.get()));
        } else {
            Query query = new MongoResmiQueryBuilder().relationSubjectId(id).query(resourceQueries.orElse(null)).pagination(pagination)
                    .sort(sort.orElse(null)).build();
            query.fields().exclude(_ID).exclude(JsonRelation._SRC_ID);
            LOG.debug("Query executed : " + query.getQueryObject().toString());
            return renameIds(find(generateRelationName(namespaceNormalizer.normalize(type), namespaceNormalizer.normalize(relationType)),
                    query));
        }
    }

    @Override
    public <T> List<T> findAll(String type, Class<T> entityClass) {
        return mongoOperations.findAll(entityClass, namespaceNormalizer.normalize(type));
    }

    public JsonArray find(String type, Query query) {
        return JsonUtils.convertToArray(mongoOperations.find(query, JsonObject.class, namespaceNormalizer.normalize(type)));
    }

    @Override
    public CountResult count(ResourceUri resourceUri, List<ResourceQuery> resourceQueries) {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(resourceUri).query(resourceQueries).build();
        if (resourceUri.isRelation()) {
            query.fields().exclude(_ID).exclude(JsonRelation._SRC_ID);
        }
        LOG.debug("Query executed : " + query.getQueryObject().toString());
        return new CountResult(mongoOperations.count(query, getMongoCollectionName(resourceUri)));
    }

    @Override
    public AverageResult average(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        aggregations.add(Aggregation.match(new MongoResmiQueryBuilder().getCriteriaFromResourceQueries(resourceQueries)));
        aggregations.add(Aggregation.group().avg(field).as("average"));

        return mongoOperations
                .aggregate(Aggregation.newAggregation(aggregations), getMongoCollectionName(resourceUri), AverageResult.class)
                .getUniqueMappedResult();
    }

    @Override
    public SumResult sum(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        aggregations.add(Aggregation.match(new MongoResmiQueryBuilder().getCriteriaFromResourceQueries(resourceQueries)));
        aggregations.add(Aggregation.group().sum(field).as("sum"));

        return mongoOperations.aggregate(Aggregation.newAggregation(aggregations), getMongoCollectionName(resourceUri), SumResult.class)
                .getUniqueMappedResult();
    }

    @Override
    public void save(String type, Object entity) {
        if (entity instanceof JsonObject) {
            JsonObject object = (JsonObject) entity;
            Optional<String> id = object.get(ID) != null ? Optional.of(object.get(ID).getAsString()) : Optional.empty();
            upsert(namespaceNormalizer.normalize(type), id, object);
        } else {
            mongoOperations.save(entity, namespaceNormalizer.normalize(type));
        }
    }

    @Override
    public void upsert(String type, String id, JsonObject entity) {
        upsert(namespaceNormalizer.normalize(type), Optional.of(id), entity);
    }

    @Override
    public boolean findAndModify(String type, String id, JsonObject entity, List<ResourceQuery> resourceQueries) {
        String collection = namespaceNormalizer.normalize(type);
        JsonElement created = entity.remove(CREATED_AT);

        Update update = updateFromJsonObject(entity, Optional.of(id), Optional.ofNullable(created));

        Query query = new MongoResmiQueryBuilder().id(id).query(resourceQueries).build();

        JsonObject saved = mongoOperations.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), JsonObject.class,
                collection);

        if (saved != null) {
            entity.addProperty(ID, id);
            return true;
        } else {
            return false;
        }
    }

    private void upsert(String collection, Optional<String> id, JsonObject entity) {
        JsonElement created = entity.remove(CREATED_AT);

        Update update = updateFromJsonObject(entity, id, Optional.ofNullable(created));

        Query query = Query.query(Criteria.where(_ID).exists(false));
        if (id.isPresent()) {
            query = Query.query(Criteria.where(_ID).is(id.get()));
        }

        JsonObject saved = mongoOperations.findAndModify(query, update, FindAndModifyOptions.options().upsert(true).returnNew(true),
                JsonObject.class, collection);

        entity.addProperty(ID, id.isPresent() ? id.get() : saved.get(ID).getAsString());

        if (created != null) {
            entity.add(CREATED_AT, created);
        }
    }

    @SuppressWarnings("unchecked")
    private Update updateFromJsonObject(JsonObject entity, Optional<String> id, Optional<JsonElement> created) {
        Update update = new Update();

        if (id.isPresent()) {
            entity.remove(ID);
            if (entity.entrySet().isEmpty()) {
                update.set(_ID, id);
            }
        }

        if (created.isPresent() && created.get().isJsonPrimitive()) {
            update.setOnInsert(CREATED_AT, GsonUtil.getPrimitive(created.get().getAsJsonPrimitive()));
        }

        jsonObjectMongoWriteConverter.convert(entity).toMap().forEach((key, value) -> update.set((String) key, value));
        entity.entrySet().stream().filter((entry) -> entry.getValue().isJsonNull()).forEach((entry) -> update.unset(entry.getKey()));
        return update;
    }

    @Override
    public void createRelation(String type, String srcId, String relation, String uri, JsonObject entity) throws NotFoundException {

        if (!exists(type, srcId)) {
            throw new NotFoundException("The resource does not exist");
        }

        JsonObject storedRelation = findRelation(type, srcId, relation, uri);
        String id = null;

        JsonObject relationJson = JsonRelation.create(srcId, uri, entity);
        if (storedRelation != null) {
            id = storedRelation.get(ID).getAsString();
            relationJson = updateRelation(storedRelation, relationJson);
        } else {
            resmiOrder.addNextOrderInRelation(type, srcId, relation, relationJson);
        }

        upsert(generateRelationName(namespaceNormalizer.normalize(type), namespaceNormalizer.normalize(relation)), Optional.ofNullable(id),
                relationJson);
    }

    @Override
    public void ensureExpireIndex(String type) {
        mongoOperations.indexOps(namespaceNormalizer.normalize(type)).ensureIndex(new Index().on(EXPIRE_AT, Direction.ASC).expire(0));
    }

    @Override
    public void ensureCollectionIndex(String type, Index index) {
        mongoOperations.indexOps(namespaceNormalizer.normalize(type)).ensureIndex(index);
    }

    @Override
    public void ensureRelationIndex(String type, String relation, Index index) {
        mongoOperations.indexOps(generateRelationName(namespaceNormalizer.normalize(type), namespaceNormalizer.normalize(relation)))
                .ensureIndex(index);
    }

    private JsonObject updateRelation(JsonObject storedRelation, JsonObject relationJson) {
        relationJson.add("id", storedRelation.get("id"));
        relationJson.add("_order", storedRelation.get("_order"));
        return relationJson;
    }

    private JsonObject findRelation(String type, String id, String relation, String uri) {
        String collection = generateRelationName(namespaceNormalizer.normalize(type), namespaceNormalizer.normalize(relation));
        Criteria criteria = Criteria.where(JsonRelation._SRC_ID).is(id).and(JsonRelation._DST_ID).is(uri);
        return mongoOperations.findOne(new Query(criteria), JsonObject.class, collection);
    }

    private String generateRelationName(String collection, String relation) {
        return collection + RELATION_CONCATENATOR + relation;
    }

    /*
     * TODO: This should be refactor out of here (alex 31.01.14)
     */
    private JsonArray renameIds(JsonArray array) {
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                renameIds(object);
            }
        }
        return array;
    }

    private JsonElement renameIds(JsonObject object) {
        object.add("id", object.get(JsonRelation._DST_ID));
        object.remove(JsonRelation._DST_ID);
        return object;
    }

    @Override
    public void deleteById(String type, String id) {
        mongoOperations.remove(new Query(Criteria.where(_ID).is(id)), namespaceNormalizer.normalize(type));
    }

    @Override
    public void deleteRelation(String type, ResourceId id, String relation, Optional<String> dstId) {
        String collection = namespaceNormalizer.normalize(type) + "." + namespaceNormalizer.normalize(relation);
        Criteria criteria = new Criteria();
        if (!id.isWildcard()) {
            criteria = criteria.and(JsonRelation._SRC_ID).is(id.getId());
        }
        if (dstId.isPresent()) {
            criteria = criteria.and(JsonRelation._DST_ID).is(dstId.get());
        }
        Query query = new Query(criteria);
        mongoOperations.remove(query, collection);
    }

    @Override
    public void moveElement(String type, String id, String relation, String uri, RelationMoveOperation relationMoveOperation) {
        resmiOrder.moveElement(type, id, relation, uri, relationMoveOperation);
    }

    private String getMongoCollectionName(ResourceUri resourceUri) {
        return Optional
                .ofNullable(namespaceNormalizer.normalize(resourceUri.getType()))
                .map(type -> type
                        + Optional.ofNullable(resourceUri.getRelation())
                                .map(relation -> RELATION_CONCATENATOR + namespaceNormalizer.normalize(relation)).orElse(EMPTY_STRING))
                .orElse(EMPTY_STRING);
    }

}
