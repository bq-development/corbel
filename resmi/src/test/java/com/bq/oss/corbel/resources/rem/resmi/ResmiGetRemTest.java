/*
 * Copyright (C) 2014 StarTIC
 */
package com.bq.oss.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.service.BadConfigurationException;
import com.bq.oss.lib.queries.request.*;
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

        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));

        when(resmiServiceMock.findResourceById(TEST_TYPE, TEST_ID)).thenReturn(json);
        Response response = getRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(json);

    }

    @Test
    public void testGetCollection() throws BadConfigurationException {
        JsonArray jsonArray = new JsonArray();
        when(resmiServiceMock.find(TEST_TYPE, collectionParametersMock)).thenReturn(jsonArray);

        when(requestParametersCollectionParametersMock.getApiParameters()).thenReturn(collectionParametersMock);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.empty());
        when(collectionParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(collectionParametersMock.getPagination()).thenReturn(paginationMock);
        when(collectionParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));

        Response response = getRem.collection(TEST_TYPE, requestParametersCollectionParametersMock, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(jsonArray);
    }

    @Test
    public void testGetCollectionCount() {
        when(resmiServiceMock.aggregate(new ResourceUri(TEST_TYPE), collectionParametersMock)).thenReturn(countResultMock);

        when(requestParametersCollectionParametersMock.getApiParameters()).thenReturn(collectionParametersMock);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(aggregationOperationMock));
        when(aggregationOperationMock.getOperator()).thenReturn(AggregationOperator.$COUNT);

        when(aggregationOperationMock.operate(Arrays.asList(resourceQueryMock))).thenReturn(Arrays.asList(resourceQueryMock));

        when(collectionParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));

        Response response = getRem.collection(TEST_TYPE, requestParametersCollectionParametersMock, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(countResultMock);
    }

    @Test
    public void testGetCollectionAverage() {
        when(resmiServiceMock.aggregate(new ResourceUri(TEST_TYPE), collectionParametersMock)).thenReturn(averageResultMock);

        when(requestParametersCollectionParametersMock.getApiParameters()).thenReturn(collectionParametersMock);
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

        when(resmiServiceMock.findRelation(TEST_TYPE, resourceIdMock, TEST_TYPE_RELATION, relationParametersMock)).thenReturn(jsonArray);

        when(requestParametersRelationParametersMock.getApiParameters()).thenReturn(relationParametersMock);
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());

        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.empty());

        Response response = getRem.relation(TEST_TYPE, resourceIdMock, TEST_TYPE_RELATION, requestParametersRelationParametersMock,
                Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(jsonArray);
    }

    @Test
    public void testGetRelationResource() throws BadConfigurationException {
        JsonArray jsonArray = new JsonArray();

        String dstId = "resourceDstId";
        when(resmiServiceMock.findRelation(TEST_TYPE, resourceIdMock, TEST_TYPE_RELATION, relationParametersMock)).thenReturn(jsonArray);

        when(requestParametersRelationParametersMock.getApiParameters()).thenReturn(relationParametersMock);
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());

        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.of(dstId));

        Response response = getRem.relation(TEST_TYPE, resourceIdMock, TEST_TYPE_RELATION, requestParametersRelationParametersMock,
                Optional.empty());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(jsonArray);
    }

    @Test
    public void testGetRelationCount() {
        when(resmiServiceMock.aggregate(new ResourceUri(TEST_TYPE, resourceIdMock.getId(), TEST_TYPE_RELATION), relationParametersMock))
                .thenReturn(countResultMock);

        when(requestParametersRelationParametersMock.getApiParameters()).thenReturn(relationParametersMock);
        when(relationParametersMock.getAggregation()).thenReturn(Optional.of(aggregationOperationMock));

        when(aggregationOperationMock.getOperator()).thenReturn(AggregationOperator.$COUNT);

        when(aggregationOperationMock.operate(Arrays.asList(resourceQueryMock))).thenReturn(Arrays.asList(resourceQueryMock));

        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(Arrays.asList(resourceQueryMock)));

        Response response = getRem.relation(TEST_TYPE, resourceIdMock, TEST_TYPE_RELATION, requestParametersRelationParametersMock,
                Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(countResultMock);
    }

    @Test
    public void testResourceNotFound() {
        when(resmiServiceMock.findResourceById(TEST_TYPE, TEST_ID)).thenReturn(null);
        Response response = getRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testNotAllowed() {
        Response response = getRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(404);
    }
}
