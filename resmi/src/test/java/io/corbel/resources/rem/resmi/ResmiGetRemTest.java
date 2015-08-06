package io.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.BadConfigurationException;
import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.AggregationOperator;
import io.corbel.lib.queries.request.AverageResult;
import io.corbel.lib.queries.request.CountResult;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Alexander De Leon
 *
 */
public class ResmiGetRemTest extends ResmiRemTest {

    private AbstractResmiRem getRem;

    @Mock RequestParameters<CollectionParameters> requestParametersCollectionParametersMock;
    @Mock RequestParameters<RelationParameters> requestParametersRelationParametersMock;
    @Mock CollectionParameters collectionParametersMock;
    @Mock RelationParameters relationParametersMock;
    @Mock ResourceQuery resourceQueryMock;
    @Mock Pagination paginationMock;
    @Mock Sort sortMock;
    @Mock CountResult countResultMock;
    @Mock AverageResult averageResultMock;
    @Mock Aggregation aggregationOperationMock;
    @Mock ResourceId resourceIdMock;

    @Override
    @Before
    public void setup() {
        super.setup();
        MockitoAnnotations.initMocks(this);
        getRem = new ResmiGetRem(resmiServiceMock);
    }

    @Test
    public void testGetResource() {
        ResourceUri resourceUri = new ResourceUri(TEST_TYPE, ID);
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));

        when(resmiServiceMock.findResource(resourceUri)).thenReturn(json);
        Response response = getRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(json);

    }

    @Test
    public void testGetCollection() throws BadConfigurationException {
        ResourceUri resourceUri = new ResourceUri(TEST_TYPE);
        JsonArray jsonArray = new JsonArray();
        when(resmiServiceMock.findCollection(resourceUri, Optional.of(collectionParametersMock))).thenReturn(jsonArray);

        when(requestParametersCollectionParametersMock.getOptionalApiParameters()).thenReturn(Optional.of(collectionParametersMock));
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.empty());
        when(collectionParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(collectionParametersMock.getPagination()).thenReturn(paginationMock);
        when(collectionParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));

        Response response = getRem.collection(TEST_TYPE, requestParametersCollectionParametersMock, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(jsonArray);
    }

    @Test
    public void testGetCollectionCount() throws BadConfigurationException {
        when(resmiServiceMock.aggregate(new ResourceUri(TEST_TYPE), collectionParametersMock)).thenReturn(countResultMock);

        when(requestParametersCollectionParametersMock.getOptionalApiParameters()).thenReturn(Optional.of(collectionParametersMock));
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(aggregationOperationMock));
        when(aggregationOperationMock.getOperator()).thenReturn(AggregationOperator.$COUNT);

        when(aggregationOperationMock.operate(Arrays.asList(resourceQueryMock))).thenReturn(Arrays.asList(resourceQueryMock));

        when(collectionParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));

        Response response = getRem.collection(TEST_TYPE, requestParametersCollectionParametersMock, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(countResultMock);
    }

    @Test
    public void testGetCollectionAverage() throws BadConfigurationException {
        when(resmiServiceMock.aggregate(new ResourceUri(TEST_TYPE), collectionParametersMock)).thenReturn(averageResultMock);

        when(requestParametersCollectionParametersMock.getOptionalApiParameters()).thenReturn(Optional.of(collectionParametersMock));
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(aggregationOperationMock));
        when(aggregationOperationMock.getOperator()).thenReturn(AggregationOperator.$AVG);

        when(aggregationOperationMock.operate(Arrays.asList(resourceQueryMock))).thenReturn(Arrays.asList(resourceQueryMock));

        when(collectionParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));

        Response response = getRem.collection(TEST_TYPE, requestParametersCollectionParametersMock, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(averageResultMock);
    }

    @Test
    public void testGetRelation() throws BadConfigurationException {
        JsonArray jsonArray = new JsonArray();
        ResourceUri resourceUri = new ResourceUri(TEST_TYPE, ID, TEST_TYPE_RELATION, null);
        ResourceId resourceId = new ResourceId(ID);

        when(resmiServiceMock.findRelation(resourceUri, Optional.of(relationParametersMock))).thenReturn(jsonArray);

        when(requestParametersRelationParametersMock.getOptionalApiParameters()).thenReturn(Optional.of(relationParametersMock));
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());

        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.empty());

        Response response = getRem.relation(TEST_TYPE, resourceId, TEST_TYPE_RELATION, requestParametersRelationParametersMock,
                Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(jsonArray);
    }

    @Test
    public void testGetRelationResource() throws BadConfigurationException {
        JsonArray jsonArray = new JsonArray();
        String dstId = "resourceDstId";
        ResourceUri resourceUri = new ResourceUri(TEST_TYPE, ID, TEST_TYPE_RELATION, dstId);
        ResourceId resourceId = new ResourceId(ID);


        when(resmiServiceMock.findRelation(resourceUri, Optional.of(relationParametersMock))).thenReturn(jsonArray);

        when(requestParametersRelationParametersMock.getOptionalApiParameters()).thenReturn(Optional.of(relationParametersMock));
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());

        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.of(dstId));

        Response response = getRem.relation(TEST_TYPE, resourceId, TEST_TYPE_RELATION, requestParametersRelationParametersMock,
                Optional.empty());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(jsonArray);
    }

    @Test
    public void testGetRelationCount() throws BadConfigurationException {
        when(resmiServiceMock.aggregate(new ResourceUri(TEST_TYPE, resourceIdMock.getId(), TEST_TYPE_RELATION), relationParametersMock))
                .thenReturn(countResultMock);

        when(requestParametersRelationParametersMock.getOptionalApiParameters()).thenReturn(Optional.of(relationParametersMock));
        when(relationParametersMock.getAggregation()).thenReturn(Optional.of(aggregationOperationMock));

        when(aggregationOperationMock.getOperator()).thenReturn(AggregationOperator.$COUNT);

        when(aggregationOperationMock.operate(Arrays.asList(resourceQueryMock))).thenReturn(Arrays.asList(resourceQueryMock));

        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.empty());

        Response response = getRem.relation(TEST_TYPE, resourceIdMock, TEST_TYPE_RELATION, requestParametersRelationParametersMock,
                Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(countResultMock);
    }

    @Test
    public void testResourceNotFound() {
        ResourceUri resourceUri = new ResourceUri(TEST_TYPE, ID);
        when(resmiServiceMock.findResource(resourceUri)).thenReturn(null);
        Response response = getRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testNotAllowed() {
        Response response = getRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(404);
    }
}
