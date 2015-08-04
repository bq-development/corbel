package com.bq.oss.corbel.resources.rem.search;

import static org.junit.Assert.assertEquals;
import io.corbel.lib.queries.exception.MalformedJsonQueryException;
import io.corbel.lib.queries.parser.CustomJsonParser;
import io.corbel.lib.queries.parser.JacksonQueryParser;
import io.corbel.lib.queries.request.ResourceQuery;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Rubén Carrasco
 *
 */
public class ElasticSearchResourceQueryBuilderTest {

    private static final String SEARCH = "search string";
    private static JacksonQueryParser queryParser;
    private static CustomJsonParser jsonParser;

    @BeforeClass
    public static void setUp() throws MalformedJsonQueryException {
        jsonParser = new CustomJsonParser(new ObjectMapper().getFactory());
        queryParser = new JacksonQueryParser(jsonParser);
    }

    @Test
    public void eqResourceQueryTest() throws MalformedJsonQueryException {
        QueryBuilder query = ElasticSearchResourceQueryBuilder.build(SEARCH, queryParser.parse("[{\"$eq\":{\"name\":\"Metallica\"}}]"));
        QueryBuilder query2 = ElasticSearchResourceQueryBuilder.build(SEARCH, queryParser.parse("[{\"name\":\"Metallica\"}]"));
        assertEquals(query.toString(), query2.toString());
        JsonNode result = jsonParser.readValueAsTree(query.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"term\":{\"name\":\"Metallica\"}}", getAndFilters(result, 0).get(0).toString());
    }

    @Test
    public void neqResourceQueryTest() throws MalformedJsonQueryException {
        List<ResourceQuery> resourceQueries = new ArrayList<ResourceQuery>();
        resourceQueries.add(queryParser.parse("[{\"$ne\":{\"name\":\"Metallica\"}}]"));
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQueries);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"not\":{\"filter\":{\"term\":{\"name\":\"Metallica\"}}}}", getAndFilters(result, 0).get(0).toString());
    }

    @Test
    public void rangesResourceQueryTest() throws MalformedJsonQueryException {
        List<ResourceQuery> resourceQueries = new ArrayList<ResourceQuery>();
        resourceQueries.add(queryParser.parse("[{\"$lt\":{\"duration\":238.0}},{\"$gte\":{\"duration\":238.0}}]"));
        resourceQueries.add(queryParser.parse("[{\"$lte\":{\"duration\":245.0}},{\"$gt\":{\"duration\":245.0}}]"));
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQueries);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"range\":{\"duration\":{\"from\":null,\"to\":238.0,\"include_lower\":true,\"include_upper\":false}}}",
                getAndFilters(result, 0).get(0).toString());
        assertEquals("{\"range\":{\"duration\":{\"from\":238.0,\"to\":null,\"include_lower\":true,\"include_upper\":true}}}",
                getAndFilters(result, 0).get(1).toString());
        assertEquals("{\"range\":{\"duration\":{\"from\":null,\"to\":245.0,\"include_lower\":true,\"include_upper\":true}}}",
                getAndFilters(result, 1).get(0).toString());
        assertEquals("{\"range\":{\"duration\":{\"from\":245.0,\"to\":null,\"include_lower\":false,\"include_upper\":true}}}",
                getAndFilters(result, 1).get(1).toString());
    }

    @Test
    public void inResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$in\":{\"categories\":[\"Metallica\"]}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"terms\":{\"categories\":[\"Metallica\"]}}", getAndFilters(result, 0).get(0).toString());
    }

    @Test
    public void ninResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$nin\":{\"categories\":[\"Metallica\"]}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"not\":{\"filter\":{\"terms\":{\"categories\":[\"Metallica\"]}}}}", getAndFilters(result, 0).get(0).toString());
    }

    @Test
    public void existsResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$exists\":{\"categories\":true}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"exists\":{\"field\":\"categories\"}}", getAndFilters(result, 0).get(0).toString());
    }

    @Test
    public void notexistsResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$exists\":{\"categories\":false}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("{\"not\":{\"filter\":{\"exists\":{\"field\":\"categories\"}}}}", getAndFilters(result, 0).get(0).toString());
    }

    @Test
    public void testEmptyResourceQuery() throws MalformedJsonQueryException {
        String queryString = "[]";
        ResourceQuery resourceQuery = queryParser.parse(queryString);
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.get("filtered").get("query").toString());
        assertEquals("[{\"and\":{\"filters\":[]}}]", getOrFilters(result).toString());
    }

    @Test
    public void testNullResourceQuery() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = null;
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.toString());
    }

    private JsonNode getAndFilters(JsonNode result, int orIndex) {
        return getOrFilters(result).get(orIndex).get("and").get("filters");
    }

    private JsonNode getOrFilters(JsonNode result) {
        return result.get("filtered").get("filter").get("or").get("filters");
    }

}
