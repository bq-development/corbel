package io.corbel.resources.rem.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.corbel.lib.queries.exception.MalformedJsonQueryException;
import io.corbel.lib.queries.parser.CustomJsonParser;
import io.corbel.lib.queries.parser.JacksonQueryParser;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.request.ResourceId;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MongoResmiQueryBuilderTest {

    public static final int LIMIT_TESTED = 10;
    public static final int OFFSET_TESTED = 10;
    public static final String SORT_DIRECTION_TESTED = "asc";
    public static final int MONGO_SORT_DIRECTION_TESTED = 1;
    public static final String SORT_FIELD_TESTED = "test";

    public static final ResourceId TEST_RESOURCE_ID = new ResourceId("1");
    public static final ResourceId TEST_RESOURCE_WILDCARD_ID = new ResourceId("_");

    public static final Pagination PAGINATION_TESTED = new Pagination(OFFSET_TESTED, LIMIT_TESTED);
    public static final Sort SORT_TESTED = new Sort(SORT_DIRECTION_TESTED, SORT_FIELD_TESTED);

    private static ResourceQuery resourceQuery;
    private static JacksonQueryParser parser;

    @BeforeClass
    public static void setUp() throws MalformedJsonQueryException {
        parser = new JacksonQueryParser(new CustomJsonParser(new ObjectMapper().getFactory()));
        String queryString = "[{\"$in\":{\"categories\":[\"Metallica\"]}}]";
        resourceQuery = parser.parse(queryString);
    }

    @Test
    public void testBuildWithId() {
        Query query = new MongoResmiQueryBuilder().id("1").build();
        assertTrue(query.getQueryObject().toMap().get("_id").equals("1"));
    }

    @Test
    public void testBuildWithIdAndPagination() {
        Query query = new MongoResmiQueryBuilder().id("1").pagination(PAGINATION_TESTED).build();
        assertTrue(query.getLimit() == LIMIT_TESTED);
        assertTrue(query.getSkip() == LIMIT_TESTED * OFFSET_TESTED);
    }

    @Test
    public void testBuildWithIdAndPaginationAndNullSort() {
        Query query = new MongoResmiQueryBuilder().id("1").pagination(PAGINATION_TESTED).sort(null).build();
        assertTrue(query.getSortObject() == null);
    }

    @Test
    public void testBuildWithIdAndPaginationAndSort() {
        Query query = new MongoResmiQueryBuilder().id("1").pagination(PAGINATION_TESTED).sort(SORT_TESTED).build();
        assertTrue(query.getSortObject().get(SORT_FIELD_TESTED).equals(MONGO_SORT_DIRECTION_TESTED));
    }

    @Test
    public void testBuildWithIdAndRelation() {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(TEST_RESOURCE_ID).build();
        assertTrue(query.getQueryObject().toMap().get("_src_id").equals("1"));
    }

    @Test
    public void testBuildWithWildCardAndRelation() {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(TEST_RESOURCE_WILDCARD_ID).build();
        assertFalse(query.getQueryObject().toMap().containsKey("_src_id"));
    }

    @Test
    public void testBuildWithRelationAndQueryAndSortAndPagination() {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(TEST_RESOURCE_ID).query(resourceQuery).pagination(PAGINATION_TESTED)
                .sort(SORT_TESTED).build();
        assertTrue(query.getQueryObject().toMap().get("_src_id").equals("1"));
        assertEquals("{\"$in\":[\"Metallica\"]}", query.getQueryObject().toMap().get("categories").toString().replace(" ", ""));
        assertTrue(query.getSortObject().get(SORT_FIELD_TESTED).equals(MONGO_SORT_DIRECTION_TESTED));
    }

    @Test
    public void testNullResourceQuery() {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(TEST_RESOURCE_ID).query((ResourceQuery) null)
                .pagination(PAGINATION_TESTED).sort(SORT_TESTED).build();
        assertTrue(query.getQueryObject().toMap().get("_src_id").equals("1"));
        assertTrue(query.getSortObject().get(SORT_FIELD_TESTED).equals(MONGO_SORT_DIRECTION_TESTED));
    }

    @Test
    public void testNullListResourceQuery() {
        Query query = new MongoResmiQueryBuilder().relationSubjectId(TEST_RESOURCE_ID).query((List) null).pagination(PAGINATION_TESTED)
                .sort(SORT_TESTED).build();
        assertTrue(query.getQueryObject().toMap().get("_src_id").equals("1"));
        assertTrue(query.getSortObject().get(SORT_FIELD_TESTED).equals(MONGO_SORT_DIRECTION_TESTED));
    }

    @Test
    public void testCreatedAtResourceQuery() throws MalformedJsonQueryException {
        String queryString = "[{\"$lt\":{\"_createdAt\":123456}}]";
        ResourceQuery resourceQuery = parser.parse(queryString);
        Query query = new MongoResmiQueryBuilder().query(resourceQuery).build();
        assertEquals("{\"$lt\":{\"$date\":\"1970-01-01T00:02:03.456Z\"}}", query.getQueryObject().toMap().get("_createdAt").toString()
                .replace(" ", ""));
    }

    @Test
    public void testUpdatedAtResourceQuery() throws MalformedJsonQueryException {
        String queryString = "[{\"$gt\":{\"_updatedAt\":123456}}]";
        ResourceQuery resourceQuery = parser.parse(queryString);
        Query query = new MongoResmiQueryBuilder().query(resourceQuery).build();
        assertEquals("{\"$gt\":{\"$date\":\"1970-01-01T00:02:03.456Z\"}}", query.getQueryObject().toMap().get("_updatedAt").toString()
                .replace(" ", ""));
    }

    @Test
    public void testWithISODateResourceQuery() throws MalformedJsonQueryException {
        String queryString = "[{\"$gt\":{\"_updatedAt\":\"ISODate(1970-01-01T00:02:03Z)\"}}]";
        ResourceQuery resourceQuery = parser.parse(queryString);
        Query query = new MongoResmiQueryBuilder().query(resourceQuery).build();
        assertEquals("{\"$gt\":{\"$date\":\"1970-01-01T00:02:03.000Z\"}}", query.getQueryObject().toMap().get("_updatedAt").toString()
                .replace(" ", ""));
    }

}
