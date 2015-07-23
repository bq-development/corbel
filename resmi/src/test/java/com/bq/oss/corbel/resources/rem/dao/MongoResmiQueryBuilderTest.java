package com.bq.oss.corbel.resources.rem.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Query;

import com.bq.oss.corbel.resources.rem.request.ResourceId;
import io.corbel.lib.queries.exception.MalformedJsonQueryException;
import io.corbel.lib.queries.parser.CustomJsonParser;
import io.corbel.lib.queries.parser.JacksonQueryParser;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
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

    @BeforeClass
    public static void setUp() throws MalformedJsonQueryException {
        JacksonQueryParser parser = new JacksonQueryParser(new CustomJsonParser(new ObjectMapper().getFactory()));
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

}
