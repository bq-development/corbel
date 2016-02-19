package io.corbel.resources.rem.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import io.corbel.lib.mongo.JsonObjectMongoWriteConverter;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.request.*;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;
import io.corbel.resources.rem.service.DefaultNamespaceNormalizer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class) public class MongoResmiDaoTest {

    private static final String DOMAIN = "DOMAIN";
    private static final String TEST_COLLECTION = "testCollection";
    private static final String DOMAIN_CONCATENATION = "__";
    private static final String TEST_COLLECTION_NAME_IN_DB = DOMAIN +  DOMAIN_CONCATENATION + TEST_COLLECTION;

    private static final String TEST_ID = "testId";
    private static final ResourceId TEST_RESOURCE_ID = new ResourceId("testId");
    private static final String TEST_REL = "testRel";
    private static final String RELATION_COLLECTION_NAME = DOMAIN + DOMAIN_CONCATENATION + TEST_COLLECTION + "." + TEST_REL;
    private static final String TEST_ID_RELATION_OBJECT = "relatedId";
    private static final int TEST_ORDER = 1;
    private static final Query TEST_QUERY = new Query();

    @Mock private MongoOperations mongoOperations;

    @Mock private  AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    private final JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter = new JsonObjectMongoWriteConverter();

    @Mock DefaultNamespaceNormalizer defaultNameNormalizer;

    @Mock ResmiOrder resmiOrderMock;

    private MongoResmiDao mongoResmiDao;


    @Before
    public void setup() {
        when(defaultNameNormalizer.normalize(anyString())).then(returnsFirstArg());
        mongoResmiDao = Mockito.spy(new MongoResmiDao(mongoOperations, jsonObjectMongoWriteConverter, defaultNameNormalizer, resmiOrderMock,
                new JsonAggregationResultsFactory(new Gson())));
    }

    @Test
    public void testFindById() {
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID);
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        when(mongoOperations.findById(TEST_ID, JsonObject.class, TEST_COLLECTION_NAME_IN_DB)).thenReturn(json);

        JsonObject object = mongoResmiDao.findResource(resourceUri);
        assertThat(object).isEqualTo(json);
    }


    @Test
    public void testFindRelation() throws InvalidApiParamException {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        json.add("_id", new JsonPrimitive("456"));
        json.add("_dst_id", new JsonPrimitive(TEST_ID_RELATION_OBJECT));
        json.add("_src_id", new JsonPrimitive(TEST_ID));
        List<JsonObject> jsonObjectList = Collections.singletonList(json);

        Pagination pagination = new Pagination(0, 10);
        String collectionName = RELATION_COLLECTION_NAME;
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoOperations.find(queryCaptor.capture(), Mockito.eq(JsonObject.class), Mockito.eq(collectionName))).thenReturn(
                jsonObjectList);

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);
        JsonElement result = mongoResmiDao.findRelation(resourceUri, Optional.empty(), Optional.of(pagination), Optional.empty());

        assertThat(result.isJsonArray()).isTrue();
        assertThat(result.getAsJsonArray().size()).isSameAs(jsonObjectList.size());
        assertThat(queryCaptor.getValue().fields().getFieldsObject().get("_id")).isEqualTo(0);
        for (JsonElement element : result.getAsJsonArray()) {
            assertThat(element.getAsJsonObject().get("id")).isEqualTo(new JsonPrimitive(TEST_ID_RELATION_OBJECT));
            assertThat(element.getAsJsonObject().has("_dst_id")).isFalse();
            assertThat(element.getAsJsonObject().has("_src_id")).isFalse();
        }
    }

    @Test
    public void testFindRelationWithWildcard() throws InvalidApiParamException {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        json.add("_id", new JsonPrimitive("456"));
        json.add("_dst_id", new JsonPrimitive(TEST_ID_RELATION_OBJECT));
        json.add("_src_id", new JsonPrimitive(TEST_ID));
        List<JsonObject> jsonObjectList = Collections.singletonList(json);

        Pagination pagination = new Pagination(0, 10);
        String collectionName = RELATION_COLLECTION_NAME;
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoOperations.find(queryCaptor.capture(), Mockito.eq(JsonObject.class), Mockito.eq(collectionName))).thenReturn(
                jsonObjectList);

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, "_", TEST_REL);
        JsonElement result = mongoResmiDao.findRelation(resourceUri, Optional.empty(), Optional.of(pagination), Optional.empty());

        assertThat(result.isJsonArray()).isTrue();
        assertThat(result.getAsJsonArray().size()).isSameAs(jsonObjectList.size());
        assertThat(queryCaptor.getValue().fields().getFieldsObject().get("_id")).isEqualTo(0);
        for (JsonElement element : result.getAsJsonArray()) {
            assertThat(element.getAsJsonObject().get("id")).isEqualTo(new JsonPrimitive(TEST_ID_RELATION_OBJECT));
            assertThat(element.getAsJsonObject().has("_dst_id")).isFalse();
            assertThat(element.getAsJsonObject().get("_src_id")).isEqualTo(new JsonPrimitive(TEST_ID));
        }
    }

    @Test
    public void testRelationWithData() throws NotFoundException {
        JsonObject json = new JsonObject();
        json.addProperty("data1", true);
        json.addProperty("data2", "data2");
        when(mongoOperations.exists(any(Query.class), anyString())).thenReturn(true);
        JsonObject jsonCounter = new JsonObject();
        jsonCounter.add("counter", new JsonPrimitive(TEST_ORDER));
        when(mongoOperations.findAndModify(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(JsonObject.class), Mockito.any()))
                .thenReturn(jsonCounter);

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("_src_id", TEST_ID);
        jsonResult.addProperty("_dst_id", TEST_ID_RELATION_OBJECT);
        jsonResult.addProperty("_order", TEST_ORDER);
        jsonResult.addProperty("data1", true);
        jsonResult.addProperty("data2", "data2");
        jsonResult.addProperty("id", "123");

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<FindAndModifyOptions> optionsCaptor = ArgumentCaptor.forClass(FindAndModifyOptions.class);

        when(mongoOperations.findAndModify(any(), any(), any(), eq(JsonObject.class), eq(RELATION_COLLECTION_NAME))).thenAnswer(
                answerWithId(jsonResult));

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL, TEST_ID_RELATION_OBJECT);
        mongoResmiDao.createRelation(resourceUri, json);

        verify(mongoOperations, times(1)).findAndModify(queryCaptor.capture(), updateCaptor.capture(), optionsCaptor.capture(),
                eq(JsonObject.class), eq(RELATION_COLLECTION_NAME));

        assertThat(optionsCaptor.getValue().isUpsert()).isTrue();

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$set")).isEqualTo(true);
        DBObject dbObjectSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(dbObjectSet.get("_src_id")).isEqualTo(TEST_ID);
        assertThat(dbObjectSet.get("_dst_id")).isEqualTo(TEST_ID_RELATION_OBJECT);
        assertThat(dbObjectSet.get("data1")).isEqualTo(true);
        assertThat(dbObjectSet.get("data2")).isEqualTo("data2");

    }

    @Test
    public void testUpsert() {
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        WriteResult writeResultMock = mock(WriteResult.class);
        when(writeResultMock.getN()).thenReturn(1);
        ArgumentCaptor<FindAndModifyOptions> optionsCaptor = ArgumentCaptor.forClass(FindAndModifyOptions.class);

        JsonObject json = new JsonObject();
        json.addProperty("a", "abc");
        json.addProperty("b", 1);
        json.add("c", null);

        when(mongoOperations.findAndModify(any(), any(), any(), eq(JsonObject.class), eq(TEST_COLLECTION))).thenAnswer(answerWithId(json));

        mongoResmiDao.updateResource(resourceUri, json);

        verify(mongoOperations).findAndModify(queryCaptor.capture(), updateCaptor.capture(), optionsCaptor.capture(),
                Mockito.eq(JsonObject.class), Mockito.eq(TEST_COLLECTION_NAME_IN_DB));

        assertThat(optionsCaptor.getValue().isUpsert()).isTrue();
        assertThat(queryCaptor.getValue().getQueryObject().get("_id")).isEqualTo(TEST_ID);

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$set")).isEqualTo(true);
        DBObject dbObjectSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(dbObjectSet.get("a")).isEqualTo("abc");
        assertThat(dbObjectSet.get("b")).isEqualTo(1);

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$unset")).isEqualTo(true);
        DBObject dbObjectUnSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$unset");
        assertThat(dbObjectUnSet.containsField("c"));
    }

    private Answer<JsonObject> answerWithId(JsonObject json) {
        return invocation -> {
            json.addProperty("_id", TEST_ID);
            return json;
        };
    }

    @Test
    public void testDelete() {
        ResourceUri uri = new ResourceUri(DOMAIN, "type", "id", "relation", "uri");
        mongoResmiDao.deleteRelation(uri, Optional.empty());
        Query query = new Query(Criteria.where("_src_id").is("id").and("_dst_id").is("uri"));
        verify(mongoOperations).findAllAndRemove(eq(query), any(), eq(DOMAIN + DOMAIN_CONCATENATION + "type.relation"));
    }

    @Test
    public void ensureCollectionIndexTest() {
        String name = "sjkdlrjasñker";
        long seconds = 10000L;

        IndexOperations indexOperations = Mockito.mock(IndexOperations.class);
        Mockito.when(mongoOperations.indexOps(Mockito.anyString())).thenReturn(indexOperations);

        Index index = new Index().on(name, Direction.ASC).expire(seconds);
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);
        mongoResmiDao.ensureIndex(resourceUri, index);

        Mockito.verify(mongoOperations).indexOps(TEST_COLLECTION_NAME_IN_DB);
        Mockito.verify(indexOperations).ensureIndex(index);
        Mockito.verifyNoMoreInteractions(mongoOperations);
    }

    @Test
    public void ensureRelationIndexTest() {
        String name = "sjkdlrjasñker";
        long seconds = 10000L;

        IndexOperations indexOperations = Mockito.mock(IndexOperations.class);
        Mockito.when(mongoOperations.indexOps(Mockito.anyString())).thenReturn(indexOperations);

        Index index = new Index().on(name, Direction.ASC).expire(seconds);
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION).setRelation(TEST_REL);
        mongoResmiDao.ensureIndex(resourceUri, index);

        Mockito.verify(mongoOperations).indexOps(RELATION_COLLECTION_NAME);
        Mockito.verify(indexOperations).ensureIndex(index);
        Mockito.verifyNoMoreInteractions(mongoOperations);
    }

    @Test
    public void averageTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("average", 10d)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.average(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("average").getAsInt()).isEqualTo(10);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void averageNotExistsFieldTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 0;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("average", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.averageResult(Optional.empty())).thenReturn(null);
        doReturn(true).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        assertTrue((mongoResmiDao.average(resourceUri, Collections.singletonList(query), testField)).getAsJsonObject().get("average").isJsonNull());
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void averageEquals0Test() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("average", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.averageResult(Optional.empty())).thenReturn(null);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.average(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("average").getAsInt()).isEqualTo(0);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$" + testField + "\"}}}]}");

    }

    @Test
    public void averageRelationTest() {
        ResourceQuery query = new ResourceQuery();
        String testField = "test";
        String field = "field";
        String value = "value";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("average", 10d)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.average(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("average").getAsInt()).isEqualTo(10);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void averageRelationNotExistsFieldTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 0;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);
        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("average", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.averageResult(Optional.empty())).thenReturn(null);
        doReturn(true).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        assertTrue((mongoResmiDao.average(resourceUri, Collections.singletonList(query), testField)).getAsJsonObject().get("average").isJsonNull());
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void averageRelationEquals0Test() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);
        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("average", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.averageResult(Optional.empty())).thenReturn(null);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.average(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("average").getAsInt()).isEqualTo(0);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$"
                        + testField + "\"}}}]}");
    }



    @Test
    public void sumTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("sum", 10d)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.sum(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("sum").getAsInt()).isEqualTo(10);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void sumNotExistsFieldTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 0;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
             new AggregationResults<>(Collections.singletonList(new BasicDBObject("sum", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.sumResult(Optional.empty())).thenReturn(null);
        doReturn(true).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        assertTrue((mongoResmiDao.sum(resourceUri, Collections.singletonList(query), testField)).getAsJsonObject().get("sum").isJsonNull());
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void sumEquals0Test() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("sum", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.sumResult(Optional.empty())).thenReturn(null);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.sum(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("sum").getAsInt()).isEqualTo(0);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void sumRelationTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("sum", 10d)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.averageResult(Optional.empty())).thenReturn(null);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.sum(resourceUri, Collections.singletonList(query), testField);

        assertThat(result.getAsJsonObject().get("sum").getAsInt()).isEqualTo(10);
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void sumRelationNotExistsFieldTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 0;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);
        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("sum", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.sumResult(Optional.empty())).thenReturn(null);
        doReturn(true).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        assertTrue((mongoResmiDao.sum(resourceUri, Collections.singletonList(query), testField)).getAsJsonObject().get("sum").isJsonNull());
        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void sumRelationEquals0Test() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";
        long countField = 2;

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);
        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("sum", 0)), new BasicDBObject()));
        Mockito.when(mongoOperations.count(any(Query.class), anyString())).thenReturn(countField);
        Mockito.when(aggregationResultsFactory.sumResult(Optional.empty())).thenReturn(null);
        doReturn(false).when(mongoResmiDao).fieldNotExists(any(ResourceUri.class),anyString(),any(List.class),anyString());

        JsonElement result = mongoResmiDao.sum(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("sum").getAsInt()).isEqualTo(0);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void maxTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("max", 10)), new BasicDBObject()));
        JsonElement result = mongoResmiDao.max(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("max").getAsInt()).isEqualTo(10);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"max\" : { \"$max\" : \"$" + testField + "\"}}}]}");
    }


    @Test
    public void maxOnEmptyCollectionTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.emptyList(), new BasicDBObject()));
        JsonElement result = mongoResmiDao.max(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("max").isJsonNull()).isTrue();
    }

    @Test
    public void maxOnNonExistentFieldTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(DOMAIN + DOMAIN_CONCATENATION + TEST_COLLECTION), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("max", null)), new BasicDBObject()));
        JsonElement result = mongoResmiDao.max(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("max").getAsJsonNull()).isEqualTo(JsonNull.INSTANCE);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"max\" : { \"$max\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void maxRelationTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("max", 10)), new BasicDBObject()));

        JsonElement result = mongoResmiDao.max(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("max").getAsInt()).isEqualTo(10);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"max\" : { \"$max\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void minTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("min", 10)), new BasicDBObject()));
        JsonElement result = mongoResmiDao.min(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("min").getAsInt()).isEqualTo(10);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"min\" : { \"$min\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void minRelationTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TEST_COLLECTION, TEST_ID, TEST_REL);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(new BasicDBObject("min", 10)), new BasicDBObject()));

        JsonElement result = mongoResmiDao.min(resourceUri, Collections.singletonList(query), testField);
        assertThat(result.getAsJsonObject().get("min").getAsInt()).isEqualTo(10);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\" , \"_src_id\" : \"" + TEST_ID + "\"}} , { \"$group\" : { \"_id\" :  null  , \"min\" : { \"$min\" : \"$"
                        + testField + "\"}}}]}");
    }

    @Test
    public void histogramTest() throws IOException {
        String testField = "test";
        ObjectMapper mapper = new ObjectMapper(); // needed for parsing json

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        BasicDBObject result = new BasicDBObject();
        result.put("_id", new BasicDBObject(testField, "t"));
        result.put("count", 1l);
        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(result), new BasicDBObject()));

        mongoResmiDao.histogram(new ResourceUri(DOMAIN, TEST_COLLECTION), Collections.emptyList(), Optional.<Pagination>empty(),
                Optional.<Sort>empty(), testField);

        JsonNode actualAggregation = mapper.readTree(argument.getValue().toString());
        JsonNode actualPipeline = actualAggregation.get("pipeline");
        JsonNode actualGroup = null;
        JsonNode actualProject = null;

        for (JsonNode node : actualPipeline) {
            if (node.has("$group")) {
                actualGroup = node.get("$group");
            }
            if (node.has("$project")) {
                actualProject = node.get("$project");
            }
        }

        assertThat(actualGroup.get("_id").asText()).isEqualTo("$" + testField);
        assertThat(actualGroup.get("ids").toString()).isEqualTo("{\"$push\":\"$_id\"}");

        assertThat(actualProject.get("count").toString()).isEqualTo("{\"$size\":\"$ids\"}");
    }

    @Test
    public void histogramTopNTest() throws IOException {
        String testField = "test";
        int n = 10;
        ObjectMapper mapper = new ObjectMapper(); // needed for parsing json

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        BasicDBObject result = new BasicDBObject();
        result.put("_id", new BasicDBObject(testField, "t"));
        result.put("count", 1l);
        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION_NAME_IN_DB), eq(DBObject.class))).thenReturn(
                new AggregationResults<>(Collections.singletonList(result), new BasicDBObject()));

        mongoResmiDao.histogram(new ResourceUri(DOMAIN, TEST_COLLECTION), Collections.emptyList(), Optional.of(new Pagination(0, n)),
                Optional.of(new Sort("desc", "count")), testField);

        JsonNode actualAggregation = mapper.readTree(argument.getValue().toString());
        JsonNode actualPipeline = actualAggregation.get("pipeline");
        JsonNode actualGroup = null;
        JsonNode actualProject = null;
        JsonNode actualSort = null;
        JsonNode actualOffset = null;
        JsonNode actualLimit = null;

        for (JsonNode node : actualPipeline) {
            if (node.has("$group")) {
                actualGroup = node.get("$group");
            }
            if (node.has("$project")) {
                actualProject = node.get("$project");
            }
            if (node.has("$sort")) {
                actualSort = node.get("$sort");
            }
            if (node.has("$skip")) {
                actualOffset = node.get("$skip");
            }
            if (node.has("$limit")) {
                actualLimit = node.get("$limit");
            }
        }

        assertThat(actualGroup.get("_id").asText()).isEqualTo("$" + testField);
        assertThat(actualGroup.get("ids").toString()).isEqualTo("{\"$push\":\"$_id\"}");

        assertThat(actualProject.get("count").toString()).isEqualTo("{\"$size\":\"$ids\"}");

        assertThat(actualSort.get("count").asInt()).isEqualTo(-1);
        assertThat(actualOffset.asInt()).isEqualTo(0);
        assertThat(actualLimit.asInt()).isEqualTo(n);
    }
}
