package io.corbel.resources.rem.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import io.corbel.lib.mongo.utils.GsonUtil;
import io.corbel.lib.queries.mongo.builder.CriteriaBuilder;
import io.corbel.lib.queries.request.*;
import io.corbel.resources.rem.dao.builder.MongoAggregationBuilder;
import io.corbel.resources.rem.model.GenericDocument;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;
import io.corbel.resources.rem.utils.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.expression.spel.SpelParseException;

import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @author Alberto J. Rubio
 *
 */
public class MongoResmiDao implements ResmiDao {

    private static final Logger LOG = LoggerFactory.getLogger(MongoResmiDao.class);

    private static final String ID = "id";
    private static final String _ID = "_id";

    private static final String RELATION_CONCATENATION = ".";
    private static final String DOMAIN_CONCATENATION = "__";
    private static final String EMPTY_STRING = "";
    private static final String EXPIRE_AT = "_expireAt";
    private static final String CREATED_AT = "_createdAt";
    private static final String COUNT = "count";
    private static final String AVERAGE = "average";


    private final MongoOperations mongoOperations;
    private final JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter;
    private final NamespaceNormalizer namespaceNormalizer;
    private final ResmiOrder resmiOrder;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    public MongoResmiDao(MongoOperations mongoOperations, JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter,
            NamespaceNormalizer namespaceNormalizer, ResmiOrder resmiOrder, AggregationResultsFactory<JsonElement> aggregationResultsFactory) {
        this.mongoOperations = mongoOperations;
        this.jsonObjectMongoWriteConverter = jsonObjectMongoWriteConverter;
        this.namespaceNormalizer = namespaceNormalizer;
        this.resmiOrder = resmiOrder;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @Override
    public boolean existsResources(ResourceUri uri) {
        return mongoOperations.exists(Query.query(Criteria.where(_ID).is(uri.getTypeId())), getMongoCollectionName(uri));
    }

    @Override
    public JsonObject findResource(ResourceUri uri) {
        return mongoOperations.findById(uri.getTypeId(), JsonObject.class, getMongoCollectionName(uri));
    }

    @Override
    public JsonArray findCollection(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Optional<Pagination> pagination,
            Optional<Sort> sort) throws InvalidApiParamException {

        Query query;
        try {
            query = new MongoResmiQueryBuilder().query(resourceQueries.orElse(null)).pagination(pagination.orElse(null))
                    .sort(sort.orElse(null)).build();
        }
        catch (PatternSyntaxException pse) {
            throw new InvalidApiParamException(pse.getMessage());
        }
        LOG.debug("findCollection Query executed : " + query.getQueryObject().toString());
        return JsonUtils.convertToArray(mongoOperations.find(query, JsonObject.class, getMongoCollectionName(uri)));
    }

    @Override
    public JsonElement findRelation(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Optional<Pagination> pagination,
            Optional<Sort> sort) throws InvalidApiParamException {
        MongoResmiQueryBuilder mongoResmiQueryBuilder = new MongoResmiQueryBuilder();

        if (uri.getRelationId() != null) {
            mongoResmiQueryBuilder.relationDestinationId(uri.getRelationId());
        }

        Query query;
        try {
            query = mongoResmiQueryBuilder.relationSubjectId(uri).query(resourceQueries.orElse(null)).pagination(pagination.orElse(null))
                    .sort(sort.orElse(null)).build();
        }
        catch (PatternSyntaxException pse) {
            throw new InvalidApiParamException(pse.getMessage());
        }
        query.fields().exclude(_ID);

        LOG.debug("findRelation Query executed : " + query.getQueryObject().toString());
        JsonArray result = renameIds(JsonUtils.convertToArray(mongoOperations.find(query, JsonObject.class, getMongoCollectionName(uri))), uri.isTypeWildcard());

        if (uri.getRelationId() != null) {
            if (result.size() == 1) {
                return result.get(0);
            } else if (result.size() == 0) {
                return null;
            }
        }

        return result;
    }

    @Override
    public JsonArray findCollectionWithGroup(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries,
            Optional<Pagination> pagination, Optional<Sort> sort, List<String> groups, boolean first) throws InvalidApiParamException {
        Aggregation aggregation = buildGroupAggregation(uri, resourceQueries, pagination, sort, groups, first);
        List<JsonObject> result = mongoOperations.aggregate(aggregation, getMongoCollectionName(uri), JsonObject.class).getMappedResults();
        return JsonUtils.convertToArray(first ? extractDocuments(result) : result);
    }

    @Override
    public JsonArray findRelationWithGroup(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries, Optional<Pagination> pagination,
            Optional<Sort> sort, List<String> groups, boolean first) throws InvalidApiParamException {
        Aggregation aggregation = buildGroupAggregation(uri, resourceQueries, pagination, sort, groups, first);
        List<JsonObject> result = mongoOperations.aggregate(aggregation, getMongoCollectionName(uri), JsonObject.class).getMappedResults();
        return renameIds(JsonUtils.convertToArray(first ? extractDocuments(result) : result), uri.isTypeWildcard());
    }

    private Aggregation buildGroupAggregation(ResourceUri uri, Optional<List<ResourceQuery>> resourceQueries,
            Optional<Pagination> pagination, Optional<Sort> sort, List<String> fields, boolean first) throws InvalidApiParamException {

        MongoAggregationBuilder builder = new MongoAggregationBuilder();
        builder.match(uri, resourceQueries);

        builder.group(fields, first);

        if (sort.isPresent()) {
            builder.sort(sort.get().getDirection().toString(), (first ? "first." : "") + sort.get().getField());
        }

        if (pagination.isPresent()) {
            builder.pagination(pagination.get());
        }

        return builder.build();
    }

    private List<JsonObject> extractDocuments(List<JsonObject> results) {
        return results.stream().map(result -> result.get(MongoAggregationBuilder.REFERENCE).getAsJsonObject()).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> findAll(ResourceUri uri, Class<T> entityClass) {
        return mongoOperations.findAll(entityClass, getMongoCollectionName(uri));
    }

    @Override
    public void updateCollection(ResourceUri uri, JsonObject entity, List<ResourceQuery> resourceQueries) {
        updateMulti(getMongoCollectionName(uri), entity, Optional.of(resourceQueries));
    }

    @Override
    public void updateResource(ResourceUri uri, JsonObject entity) {
        findAndModify(getMongoCollectionName(uri), Optional.of(uri.getTypeId()), entity, true, Optional.empty());
    }

    @Override
    public boolean conditionalUpdateResource(ResourceUri uri, JsonObject entity, List<ResourceQuery> resourceQueries) {
        JsonObject saved = findAndModify(getMongoCollectionName(uri), Optional.of(uri.getTypeId()), entity, false,
                Optional.of(resourceQueries));
        return saved != null;
    }

    private void updateMulti(String collection, JsonObject entity, Optional<List<ResourceQuery>> resourceQueries) {
        Update update = updateFromJsonObject(entity, Optional.empty());
        Query query = getQueryFromResourceQuery(resourceQueries, Optional.empty());
        mongoOperations.updateMulti(query, update, JsonObject.class, collection);
    }

    private Query getQueryFromResourceQuery(Optional<List<ResourceQuery>> resourceQueries, Optional<String> id) {

        MongoResmiQueryBuilder builder = id.map(identifier -> new MongoResmiQueryBuilder().id(identifier)).orElse(
                new MongoResmiQueryBuilder());

        if (resourceQueries.isPresent()) {
            builder.query(resourceQueries.get());
        }

        return builder.build();
    }

    private JsonObject findAndModify(String collection, Optional<String> id, JsonObject entity, boolean upsert,
            Optional<List<ResourceQuery>> resourceQueries) {

        Update update = updateFromJsonObject(entity, id);
        Query query = (id.isPresent()) ? getQueryFromResourceQuery(resourceQueries, id) : Query.query(Criteria.where(_ID).exists(false));

        return mongoOperations.findAndModify(query, update, FindAndModifyOptions.options().upsert(upsert).returnNew(true),
                JsonObject.class, collection);
    }

    @Override
    public void saveResource(ResourceUri uri, Object entity) {
        mongoOperations.save(entity, getMongoCollectionName(uri));
    }

    @SuppressWarnings("unchecked")
    private Update updateFromJsonObject(JsonObject entity, Optional<String> id) {
        Update update = new Update();

        if (id.isPresent()) {
            entity.remove(ID);
            if (entity.entrySet().isEmpty()) {
                update.set(_ID, id);
            }
        }

        if (entity.has(CREATED_AT)) {
            JsonPrimitive createdAt = entity.get(CREATED_AT).getAsJsonPrimitive();
            entity.remove(CREATED_AT);
            update.setOnInsert(CREATED_AT, GsonUtil.getPrimitive(createdAt));
        }

        jsonObjectMongoWriteConverter.convert(entity).toMap().forEach((key, value) -> update.set((String) key, value));
        entity.entrySet().stream().filter(entry -> entry.getValue().isJsonNull()).forEach(entry -> update.unset(entry.getKey()));

        return update;
    }

    @Override
    public void createRelation(ResourceUri uri, JsonObject entity) throws NotFoundException {
        if (!existsResources(new ResourceUri(uri.getDomain(), uri.getType(), uri.getTypeId()))) {
            throw new NotFoundException("The resource does not exist");
        }

        JsonObject relationJson = JsonRelation.create(uri.getTypeId(), uri.getRelationId(), entity);
        JsonObject storedRelation = findModifyOrCreateRelation(uri, relationJson);

        if (!storedRelation.has("_order")) {
            JsonObject order = new JsonObject();
            resmiOrder.addNextOrderInRelation(uri, order);
            findAndModify(getMongoCollectionName(uri), Optional.ofNullable(storedRelation.get("id").getAsString()), order, false,
                    Optional.empty());
        }
    }

    private JsonObject findModifyOrCreateRelation(ResourceUri uri, JsonObject entity) {
        if (uri.getRelationId() != null) {
            Criteria criteria = Criteria.where(JsonRelation._SRC_ID).is(uri.getTypeId()).and(JsonRelation._DST_ID).is(uri.getRelationId());
            Update update = updateFromJsonObject(entity, Optional.<String>empty());
            update.set(JsonRelation._SRC_ID, uri.getTypeId());
            update.set(JsonRelation._DST_ID, uri.getRelationId());
            return mongoOperations.findAndModify(new Query(criteria), update, FindAndModifyOptions.options().upsert(true).returnNew(true),
                    JsonObject.class, getMongoCollectionName(uri));
        } else {
            mongoOperations.save(entity, getMongoCollectionName(uri));
            return entity;
        }
    }

    @Override
    public void ensureExpireIndex(ResourceUri uri) {
        mongoOperations.indexOps(getMongoCollectionName(uri)).ensureIndex(new Index().on(EXPIRE_AT, Direction.ASC).expire(0));
    }

    @Override
    public void ensureIndex(ResourceUri uri, Index index) {
        mongoOperations.indexOps(getMongoCollectionName(uri)).ensureIndex(index);
    }

    /*
     * TODO: This should be refactor out of here (alex 31.01.14)
     */
    private JsonArray renameIds(JsonArray array, boolean wildcard) {
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                renameIds(object, wildcard);
            }
        }
        return array;
    }

    private JsonElement renameIds(JsonObject object, boolean wildcard) {
        object.add("id", object.get(JsonRelation._DST_ID));
        object.remove(JsonRelation._DST_ID);
        if(!wildcard) {
            object.remove(JsonRelation._SRC_ID);
        }
        return object;
    }

    @Override
    public JsonObject deleteResource(ResourceUri uri) {
        Criteria criteria = Criteria.where(_ID).is(uri.getTypeId());
        return findAndRemove(uri, criteria);
    }

    @Override
    public List<GenericDocument> deleteCollection(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        List<ResourceQuery> resourceQueries = queries.orElse(Collections.<ResourceQuery>emptyList());
        Criteria criteria = CriteriaBuilder.buildFromResourceQueries(resourceQueries);
        return findAllAndRemove(uri, criteria);
    }

    @Override
    public List<GenericDocument> deleteRelation(ResourceUri uri, Optional<List<ResourceQuery>> queries) {
        List<ResourceQuery> resourceQueries = queries.orElse(Collections.<ResourceQuery>emptyList());
        Criteria criteria = CriteriaBuilder.buildFromResourceQueries(resourceQueries);
        if (!uri.isTypeWildcard()) {
            criteria = criteria.and(JsonRelation._SRC_ID).is(uri.getTypeId());
        }
        if (uri.getRelationId() != null) {
            criteria = criteria.and(JsonRelation._DST_ID).is(uri.getRelationId());
        }

        return findAllAndRemove(uri, criteria);
    }

    private List<GenericDocument> findAllAndRemove(ResourceUri resourceUri, Criteria criteria) {
        return mongoOperations.findAllAndRemove(new Query(criteria), GenericDocument.class, getMongoCollectionName(resourceUri));
    }

    private JsonObject findAndRemove(ResourceUri resourceUri, Criteria criteria) {
        return mongoOperations.findAndRemove(new Query(criteria), JsonObject.class, getMongoCollectionName(resourceUri));
    }


    @Override
    public void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation) {
        resmiOrder.moveRelation(uri, relationMoveOperation);
    }


    @Override
    public JsonElement count(ResourceUri resourceUri, List<ResourceQuery> resourceQueries) {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(resourceUri).query(resourceQueries).build();
        if (resourceUri.isRelation()) {
            query.fields().exclude(_ID).exclude(JsonRelation._SRC_ID);
        }
        LOG.debug("Query executed : " + query.getQueryObject().toString());
        return aggregationResultsFactory.countResult(mongoOperations.count(query, getMongoCollectionName(resourceUri)));
    }

    @Override
    public JsonElement average(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<DBObject> results = aggregate(resourceUri, resourceQueries, group().avg(field).as(AVERAGE));

        return fieldNotExists(resourceUri, field, results,AVERAGE)? aggregationResultsFactory.averageResult(Optional.empty()):
                aggregationResultsFactory.averageResult(results.isEmpty() ? Optional.empty() : Optional.ofNullable(
                        (Number) results.get(0).get(AVERAGE)).map(Number::doubleValue));
    }


    @Override
    public JsonElement sum(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<DBObject> results = aggregate(resourceUri, resourceQueries, group().sum(field).as("sum"));

         return fieldNotExists(resourceUri, field, results,"sum")? aggregationResultsFactory.sumResult(Optional.empty()):
                 aggregationResultsFactory.sumResult(results.isEmpty() ? Optional.empty() : Optional.ofNullable(
                         (Number) results.get(0).get("sum")).map(Number::doubleValue));
    }

    protected boolean fieldNotExists(ResourceUri resourceUri, String field, List<DBObject> results,String type) {
        Query query = Query.query(Criteria.where(field).exists(true));

        return ((results.get(0).get(type).equals(0) || results.get(0).get(type).equals(0.0)) && mongoOperations.count(query, getMongoCollectionName(resourceUri)) == 0);
    }

    @Override
    public JsonElement max(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<DBObject> results = aggregate(resourceUri, resourceQueries, group().max(field).as("max"));
        return aggregationResultsFactory.maxResult(results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0).get("max")));
    }

    @Override
    public JsonElement min(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, String field) {
        List<DBObject> results = aggregate(resourceUri, resourceQueries, group().min(field).as("min"));
        return aggregationResultsFactory.minResult(results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0).get("min")));
    }

    @Override
    public JsonArray combine(ResourceUri resourceUri, Optional<List<ResourceQuery>> resourceQueries, Optional<Pagination> pagination,
            Optional<Sort> sort, String field, String expression) throws InvalidApiParamException {

        MongoAggregationBuilder builder = new MongoAggregationBuilder();
        builder.match(resourceUri, resourceQueries);

        builder.projection(field, expression);

        if (sort.isPresent()) {
            builder.sort(sort.get().getDirection().toString(), sort.get().getField());
        }

        if (pagination.isPresent()) {
            builder.pagination(pagination.get());
        }

        Aggregation aggregation = builder.build();

        List<JsonObject> results;
        try {
            results = mongoOperations.aggregate(aggregation, getMongoCollectionName(resourceUri), JsonObject.class)
                    .getMappedResults();
        }
        catch (SpelParseException spe) {
            throw new InvalidApiParamException(spe.getMessage());
        }

        return JsonUtils.convertToArray(results.stream().map(result -> {
            JsonObject document = result.get(MongoAggregationBuilder.DOCUMENT).getAsJsonObject();
            document.add(field, result.get(field));
            return document;
        }).collect(Collectors.toList()));
    }

    @Override
    public JsonElement histogram(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, Optional<Pagination> pagination,
            Optional<Sort> sortParam, String field) {
        AggregationOperation[] aggregations = {
                group(Fields.from(Fields.field(field, field))).push("$_id").as("ids"),
                new ExposingFieldsCustomAggregationOperation(new BasicDBObject("$project", new BasicDBObject(COUNT, new BasicDBObject(
                        "$size", "$ids")))) {
                    @Override
                    public ExposedFields getFields() {
                        return ExposedFields.synthetic(Fields.fields(COUNT));
                    }
                }};

        if (sortParam.isPresent()) {
            Direction direction = Direction.ASC;
            if (sortParam.get().getDirection() == Sort.Direction.DESC) {
                direction = Direction.DESC;
            }
            aggregations = ArrayUtils.add(aggregations, sort(direction, sortParam.get().getField()));
        }

        if (pagination.isPresent()) {
            aggregations = ArrayUtils.addAll(aggregations, skip(pagination.get().getPage() * pagination.get().getPageSize()),
                    limit(pagination.get().getPageSize()));
        }

        List<HistogramEntry> results = aggregate(resourceUri, resourceQueries, aggregations).stream()
                .map(result -> toHistogramEntry(result, field)).collect(Collectors.toList());
        return aggregationResultsFactory.histogramResult(results.toArray(new HistogramEntry[results.size()]));
    }

    private HistogramEntry toHistogramEntry(DBObject result, String... fields) {
        long count = ((Number) result.get(COUNT)).longValue();
        Map<String, Object> values;
        if (fields.length == 1) {
            values = Collections.singletonMap(fields[0], result.get("_id"));
        } else {
            values = new HashMap<>(fields.length);
            result.keySet().stream().filter(f -> !COUNT.equals(f)).forEach(f -> values.put(f, result.get(f)));
        }

        return new HistogramEntry(count, values);
    }

    private List<DBObject> aggregate(ResourceUri resourceUri, List<ResourceQuery> resourceQueries, AggregationOperation... operations) {
        Criteria criterias = CriteriaBuilder.buildFromResourceQueries(resourceQueries);
        if (resourceUri.isRelation() && !resourceUri.isTypeWildcard()) {
            criterias = criterias.and(JsonRelation._SRC_ID).is(resourceUri.getTypeId());
        }
        List<AggregationOperation> aggregations = new ArrayList<>(operations.length + 1);
        aggregations.add(Aggregation.match(criterias));
        aggregations.addAll(Arrays.asList(operations));
        return mongoOperations.aggregate(newAggregation(aggregations), getMongoCollectionName(resourceUri), DBObject.class)
                .getMappedResults();
    }

    private String getMongoCollectionName(ResourceUri resourceUri) {
        return  namespaceNormalizer.normalize(resourceUri.getDomain()) + DOMAIN_CONCATENATION +
                Optional.ofNullable(namespaceNormalizer.normalize(resourceUri.getType()))
                .map(type -> type + Optional.ofNullable(resourceUri.getRelation())
                        .map(relation -> RELATION_CONCATENATION + namespaceNormalizer.normalize(relation)).orElse(EMPTY_STRING))
                .orElse(EMPTY_STRING);
    }

    private class CustomAggregationOperation implements AggregationOperation {
        private final DBObject operation;

        public CustomAggregationOperation(DBObject operation) {
            this.operation = operation;
        }

        @Override
        public DBObject toDBObject(AggregationOperationContext context) {
            return context.getMappedObject(operation);
        }
    }

    private class ExposingFieldsCustomAggregationOperation extends CustomAggregationOperation implements FieldsExposingAggregationOperation {

        public ExposingFieldsCustomAggregationOperation(DBObject operation) {
            super(operation);
        }

        @Override
        public ExposedFields getFields() {
            return null;
        }
    }
}
