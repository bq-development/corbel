package io.corbel.resources.rem.dao.builder;

import static org.junit.Assert.assertEquals;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.resmi.exception.MongoAggregationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

/**
 * @author Rubén Carrasco
 *
 */
public class MongoAggregationBuilderTest {

    private static final String FIELD_1 = "field_1";
    private static final String FIELD_2 = "field_2";
    private static final String ASC = "ASC";
    private static final String VALUE = "value";
    MongoAggregationBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new MongoAggregationBuilder();
    }

    @Test
    public void testMatch() throws MongoAggregationException {
        List<ResourceQuery> resourceQueries = new ArrayList<>();
        resourceQueries.add(new ResourceQueryBuilder().add(FIELD_1, VALUE).build());
        Aggregation agg = builder.match(new ResourceUri("testType", "testRes", "testRel"), Optional.of(resourceQueries)).build();
        assertEquals(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"field_1\" : \"value\" , \"_src_id\" : \"testRes\"}}]}",
                agg.toString());
    }

    @Test
    public void testSort() throws MongoAggregationException {
        Aggregation agg = builder.sort(ASC, FIELD_1).build();
        assertEquals("{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$sort\" : { \"field_1\" : 1}}]}", agg.toString());
    }

    @Test
    public void testGroup() throws MongoAggregationException {
        List<String> fields = new ArrayList<>();
        fields.add(FIELD_1);
        Aggregation agg = builder.group(fields).build();
        assertEquals("{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$group\" : { \"_id\" : \"$field_1\"}}]}", agg.toString());
    }

    @Test
    public void testPagination() throws MongoAggregationException {
        Pagination pagination = new Pagination(0, 50);
        Aggregation agg = builder.pagination(pagination).build();
        assertEquals("{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$skip\" : 0} , { \"$limit\" : 50}]}", agg.toString());
    }

    @Test
    public void testPipeline() throws MongoAggregationException {
        Pagination pagination = new Pagination(0, 50);
        List<String> fields = new ArrayList<>();
        fields.add(FIELD_1);
        List<ResourceQuery> resourceQueries = new ArrayList<>();
        resourceQueries.add(new ResourceQueryBuilder().add(FIELD_1, VALUE).build());
        Aggregation agg = builder.match(new ResourceUri("testType", "testRes", "testRel"), Optional.of(resourceQueries)).sort(ASC, FIELD_1)
                .group(fields).pagination(pagination).build();
        assertEquals(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$match\" : { \"field_1\" : \"value\" , \"_src_id\" : \"testRes\"}} , { \"$sort\" : { \"field_1\" : 1}} , { \"$group\" : { \"_id\" : \"$field_1\"}} , { \"$skip\" : 0} , { \"$limit\" : 50}]}",
                agg.toString());
    }

    @Test
    public void testGroupWithMultipleFields() throws MongoAggregationException {
        List<String> fields = new ArrayList<>();
        fields.add(FIELD_1);
        fields.add(FIELD_2);

        Aggregation agg = builder.group(fields).build();
        assertEquals(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$group\" : { \"_id\" : { \"field_1\" : \"$field_1\" , \"field_2\" : \"$field_2\"}}}]}",
                agg.toString());
    }

    @Test
    public void testGroupWithFirst() throws MongoAggregationException {
        List<String> fields = new ArrayList<>();
        fields.add(FIELD_1);

        Aggregation agg = builder.group(fields, true).build();
        assertEquals(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$group\" : { \"_id\" : \"$field_1\" , \"first\" : { \"$first\" : \"$$ROOT\"}}}]}",
                agg.toString());
    }

    @Test
    public void testMultiplyProjection() throws MongoAggregationException {
        Aggregation agg = builder.projection("multiply", "field2 * field3").build();
        assertEquals(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$project\" : { \"document\" : \"$$ROOT\" , \"multiply\" : { \"$multiply\" : [ \"$field2\" , \"$field3\"]}}}]}",
                agg.toString());
    }

    @Test
    public void testSumProjection() throws MongoAggregationException {
        Aggregation agg = builder.projection("sum", "field2 + field3").build();
        assertEquals(
                "{ \"aggregate\" : \"__collection__\" , \"pipeline\" : [ { \"$project\" : { \"document\" : \"$$ROOT\" , \"sum\" : { \"$add\" : [ \"$field2\" , \"$field3\"]}}}]}",
                agg.toString());
    }

    @Test(expected = MongoAggregationException.class)
    public void testNoOperations() throws MongoAggregationException {
        builder.build();
    }

}
