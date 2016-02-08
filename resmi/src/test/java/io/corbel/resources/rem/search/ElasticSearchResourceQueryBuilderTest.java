package io.corbel.resources.rem.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.corbel.lib.queries.exception.MalformedJsonQueryException;
import io.corbel.lib.queries.parser.CustomJsonParser;
import io.corbel.lib.queries.parser.JacksonQueryParser;
import io.corbel.lib.queries.request.ResourceQuery;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
        assertEquals("{\"term\":{\"name\":\"Metallica\"}}", jsonNode.get("bool").get("filter").toString());
    }

    @Test
    public void neqResourceQueryTest() throws MalformedJsonQueryException {
        List<ResourceQuery> resourceQueries = new ArrayList<ResourceQuery>();
        resourceQueries.add(queryParser.parse("[{\"$ne\":{\"name\":\"Metallica\"}}]"));
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQueries);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
        assertEquals("{\"term\":{\"name\":\"Metallica\"}}", jsonNode.get("bool").get("must_not").toString());
    }

    @Test
    public void rangesResourceQueryTest() throws MalformedJsonQueryException {
        List<ResourceQuery> resourceQueries = new ArrayList<ResourceQuery>();
        resourceQueries.add(queryParser.parse("[{\"$lt\":{\"duration\":238.0}},{\"$gte\":{\"duration\":238.0}}]"));
        resourceQueries.add(queryParser.parse("[{\"$lte\":{\"duration\":245.0}},{\"$gt\":{\"duration\":245.0}}]"));
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQueries);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");

        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get(0).get("bool").get("must").toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get(1).get("bool").get("must").toString());


        JsonNode ranges = jsonNode.get(0).get("bool").get("filter");

        assertEquals("{\"range\":{\"duration\":{\"from\":null,\"to\":238.0,\"include_lower\":true,\"include_upper\":false}}}",
                ranges.get(0).toString());
        assertEquals("{\"range\":{\"duration\":{\"from\":238.0,\"to\":null,\"include_lower\":true,\"include_upper\":true}}}",
                ranges.get(1).toString());

        ranges = jsonNode.get(1).get("bool").get("filter");
        assertEquals("{\"range\":{\"duration\":{\"from\":null,\"to\":245.0,\"include_lower\":true,\"include_upper\":true}}}",
                ranges.get(0).toString());
        assertEquals("{\"range\":{\"duration\":{\"from\":245.0,\"to\":null,\"include_lower\":false,\"include_upper\":true}}}",
                ranges.get(1).toString());
    }

    @Test
    public void inResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$in\":{\"categories\":[\"Metallica\"]}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
        assertEquals("{\"terms\":{\"categories\":[\"Metallica\"]}}", jsonNode.get("bool").get("filter").toString());
    }

    @Test
    public void ninResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$nin\":{\"categories\":[\"Metallica\"]}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
        assertEquals("{\"terms\":{\"categories\":[\"Metallica\"]}}", jsonNode.get("bool").get("must_not").toString());
    }

    @Test
    public void existsResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$exists\":{\"categories\":true}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
        assertEquals("{\"exists\":{\"field\":\"categories\"}}", jsonNode.get("bool").get("filter").toString());
    }

    @Test
    public void notexistsResourceQueryTest() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = queryParser.parse("[{\"$exists\":{\"categories\":false}}]");
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
        assertEquals("{\"exists\":{\"field\":\"categories\"}}", jsonNode.get("bool").get("must_not").toString());
    }

    @Test
    public void testEmptyResourceQuery() throws MalformedJsonQueryException {
        String queryString = "[]";
        ResourceQuery resourceQuery = queryParser.parse(queryString);
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        JsonNode jsonNode = result.get("bool").get("should");
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", jsonNode.get("bool").get("must").toString());
    }

    @Test
    public void testNullResourceQuery() throws MalformedJsonQueryException {
        ResourceQuery resourceQuery = null;
        QueryBuilder builder = ElasticSearchResourceQueryBuilder.build(SEARCH, resourceQuery);
        JsonNode result = jsonParser.readValueAsTree(builder.toString());
        assertEquals("{\"query_string\":{\"query\":\"search string\"}}", result.toString());
    }
}
