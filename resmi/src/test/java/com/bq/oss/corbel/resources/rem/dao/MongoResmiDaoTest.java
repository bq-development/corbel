package com.bq.oss.corbel.resources.rem.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
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

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.service.DefaultNamespaceNormalizer;
import com.bq.oss.lib.mongo.JsonObjectMongoWriteConverter;
import com.bq.oss.lib.queries.QueryNodeImpl;
import com.bq.oss.lib.queries.StringQueryLiteral;
import com.bq.oss.lib.queries.request.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

@RunWith(MockitoJUnitRunner.class)
public class MongoResmiDaoTest {

    private static final String TEST_COLLECTION = "testCollection";
    private static final String TEST_ID = "testId";
    private static final ResourceId TEST_RESOURCE_ID = new ResourceId("testId");
    private static final String TEST_REL = "testRel";
    private static final String RELATION_COLLECTION_NAME = TEST_COLLECTION + "." + TEST_REL;
    private static final String TEST_ID_RELATION_OBJECT = "relatedId";
    private static final int TEST_ORDER = 1;
    private static final Query TEST_QUERY = new Query();

    @Mock private MongoOperations mongoOperations;

    private final JsonObjectMongoWriteConverter jsonObjectMongoWriteConverter = new JsonObjectMongoWriteConverter();

    @Mock DefaultNamespaceNormalizer defaultNameNormalizer;

    @Mock ResmiOrder resmiOrderMock;

    private MongoResmiDao mongoResmiDao;

    @Before
    public void setup() {
        when(defaultNameNormalizer.normalize(anyString())).then(returnsFirstArg());
        mongoResmiDao = new MongoResmiDao(mongoOperations, jsonObjectMongoWriteConverter, defaultNameNormalizer, resmiOrderMock);
    }

    @Test
    public void testFindById() {
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID);
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        when(mongoOperations.findById(TEST_ID, JsonObject.class, TEST_COLLECTION)).thenReturn(json);

        JsonObject object = mongoResmiDao.findResource(resourceUri);
        assertThat(object).isEqualTo(json);
    }


    @Test
    public void testFindRelation() {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        json.add("_id", new JsonPrimitive("456"));
        json.add("_dst_id", new JsonPrimitive(TEST_ID_RELATION_OBJECT));
        List<JsonObject> jsonObjectList = Arrays.asList(json);

        Pagination pagination = new Pagination(0, 10);
        String collectionName = RELATION_COLLECTION_NAME;
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoOperations.find(queryCaptor.capture(), Mockito.eq(JsonObject.class), Mockito.eq(collectionName))).thenReturn(
                jsonObjectList);

        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID, TEST_REL);
        JsonElement result = mongoResmiDao.findRelation(resourceUri, Optional.empty(), Optional.of(pagination), Optional.empty());

        assertThat(result.isJsonArray()).isTrue();
        assertThat(result.getAsJsonArray().size()).isSameAs(jsonObjectList.size());
        assertThat(queryCaptor.getValue().fields().getFieldsObject().get("_src_id")).isEqualTo(0);
        assertThat(queryCaptor.getValue().fields().getFieldsObject().get("_id")).isEqualTo(0);
        for (JsonElement element : result.getAsJsonArray()) {
            assertThat(element.getAsJsonObject().get("id")).isEqualTo(new JsonPrimitive(TEST_ID_RELATION_OBJECT));
            assertThat(element.getAsJsonObject().has("_dst_id")).isFalse();
        }
    }

    @Test
    public void testCreateRelationWithCreatedAt() throws NotFoundException {
        JsonObject json = new JsonObject();
        json.addProperty("data1", true);
        json.addProperty("data2", "data2");
        json.addProperty("_createdAt", "date");

        when(mongoOperations.exists(any(Query.class), anyString())).thenReturn(true);

        JsonObject jsonCounter = new JsonObject();
        jsonCounter.add("counter", new JsonPrimitive(TEST_ORDER));
        when(mongoOperations.findAndModify(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(JsonObject.class), Mockito.any()))
                .thenReturn(jsonCounter);

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("_src_id", TEST_ID);
        jsonResult.addProperty("_dst_id", TEST_ID_RELATION_OBJECT);
        jsonResult.addProperty("_order", TEST_ORDER);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<FindAndModifyOptions> optionsCaptor = ArgumentCaptor.forClass(FindAndModifyOptions.class);

        when(mongoOperations.findAndModify(any(), any(), any(), eq(JsonObject.class), eq(RELATION_COLLECTION_NAME))).thenAnswer(
                answerWithId(jsonResult));

        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID, TEST_REL, TEST_ID_RELATION_OBJECT);
        mongoResmiDao.createRelation(resourceUri, json);

        verify(mongoOperations).findAndModify(queryCaptor.capture(), updateCaptor.capture(), optionsCaptor.capture(), eq(JsonObject.class),
                eq(RELATION_COLLECTION_NAME));

        assertThat(optionsCaptor.getValue().isUpsert()).isTrue();

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$set")).isEqualTo(true);
        DBObject dbObjectSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(dbObjectSet.get("_src_id")).isEqualTo(TEST_ID);
        assertThat(dbObjectSet.get("_dst_id")).isEqualTo(TEST_ID_RELATION_OBJECT);

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$setOnInsert")).isEqualTo(true);
        dbObjectSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$setOnInsert");
        assertThat(dbObjectSet.get("_createdAt")).isEqualTo("date");
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

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<FindAndModifyOptions> optionsCaptor = ArgumentCaptor.forClass(FindAndModifyOptions.class);

        when(mongoOperations.findAndModify(any(), any(), any(), eq(JsonObject.class), eq(RELATION_COLLECTION_NAME))).thenAnswer(
                answerWithId(jsonResult));

        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID, TEST_REL, TEST_ID_RELATION_OBJECT);
        mongoResmiDao.createRelation(resourceUri, json);

        verify(mongoOperations).findAndModify(queryCaptor.capture(), updateCaptor.capture(), optionsCaptor.capture(), eq(JsonObject.class),
                eq(RELATION_COLLECTION_NAME));

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
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID);

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
                Mockito.eq(JsonObject.class), Mockito.eq(TEST_COLLECTION));

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

    @Test
    public void testUpsertWithCreatedAt() {
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<FindAndModifyOptions> optionsCaptor = ArgumentCaptor.forClass(FindAndModifyOptions.class);

        JsonObject json = new JsonObject();
        json.addProperty("a", "abc");
        json.addProperty("_createdAt", "date");

        when(mongoOperations.findAndModify(any(), any(), any(), eq(JsonObject.class), eq(TEST_COLLECTION))).thenAnswer(answerWithId(json));

        mongoResmiDao.updateResource(resourceUri, json);

        verify(mongoOperations).findAndModify(queryCaptor.capture(), updateCaptor.capture(), optionsCaptor.capture(),
                Mockito.eq(JsonObject.class), Mockito.eq(TEST_COLLECTION));

        assertThat(optionsCaptor.getValue().isUpsert()).isTrue();
        assertThat(queryCaptor.getValue().getQueryObject().get("_id")).isEqualTo(TEST_ID);

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$set")).isEqualTo(true);
        DBObject dbObjectSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(dbObjectSet.get("a")).isEqualTo("abc");

        assertThat(updateCaptor.getValue().getUpdateObject().containsField("$setOnInsert")).isEqualTo(true);
        dbObjectSet = (DBObject) updateCaptor.getValue().getUpdateObject().get("$setOnInsert");
        assertThat(dbObjectSet.get("_createdAt")).isEqualTo("date");
    }

    private Answer<JsonObject> answerWithId(JsonObject json) {
        return new Answer<JsonObject>() {

            @Override
            public JsonObject answer(InvocationOnMock invocation) throws Throwable {
                json.addProperty("id", TEST_ID);
                return json;
            }
        };
    }

    @Test
    public void testDelete() {
        ResourceUri uri = new ResourceUri("type", "id", "relation", "uri");
        mongoResmiDao.deleteRelation(uri);
        Query query = new Query(Criteria.where("_src_id").is("id").and("_dst_id").is("uri"));
        verify(mongoOperations).findAllAndRemove(eq(query), any(), eq("type.relation"));
    }

    @Test
    public void ensureCollectionIndexTest() {
        String name = "sjkdlrjasñker";
        long seconds = 10000L;

        IndexOperations indexOperations = Mockito.mock(IndexOperations.class);
        Mockito.when(mongoOperations.indexOps(Mockito.anyString())).thenReturn(indexOperations);

        Index index = new Index().on(name, Direction.ASC).expire(seconds);
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION);
        mongoResmiDao.ensureIndex(resourceUri, index);

        Mockito.verify(mongoOperations).indexOps(TEST_COLLECTION);
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
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION).setRelation(TEST_REL);
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
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION), eq(AverageResult.class))).thenReturn(
                new AggregationResults<AverageResult>(Arrays.asList(new AverageResult(10)), new BasicDBObject()));
        AverageResult result = mongoResmiDao.average(resourceUri, Arrays.asList(query), testField);
        assertThat(result.getAverage()).isEqualTo(10);

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

        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_RESOURCE_ID.toString(), TEST_REL);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(RELATION_COLLECTION_NAME), eq(AverageResult.class))).thenReturn(
                new AggregationResults<AverageResult>(Arrays.asList(new AverageResult(10)), new BasicDBObject()));

        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        AverageResult result = mongoResmiDao.average(resourceUri, Arrays.asList(query), testField);
        assertThat(result.getAverage()).isEqualTo(10);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"average\" : { \"$avg\" : \"$" + testField + "\"}}}]}");
    }

    @Test
    public void sumTest() {
        ResourceQuery query = new ResourceQuery();
        String field = "field";
        String value = "value";
        String testField = "test";

        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION);

        ArgumentCaptor<Aggregation> argument = ArgumentCaptor.forClass(Aggregation.class);
        query.addQueryNode(new QueryNodeImpl(QueryOperator.$EQ, field, new StringQueryLiteral(value)));

        Mockito.when(mongoOperations.aggregate(argument.capture(), eq(TEST_COLLECTION), eq(SumResult.class))).thenReturn(
                new AggregationResults<SumResult>(Arrays.asList(new SumResult(10)), new BasicDBObject()));
        SumResult result = mongoResmiDao.sum(resourceUri, Arrays.asList(query), testField);
        assertThat(result.getSum()).isEqualTo(10);

        assertThat(argument.getValue().toString()).isEqualTo(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"" + field + "\" : \"" + value
                        + "\"}} , { \"$group\" : { \"_id\" :  null  , \"sum\" : { \"$sum\" : \"$" + testField + "\"}}}]}");
    }
}
