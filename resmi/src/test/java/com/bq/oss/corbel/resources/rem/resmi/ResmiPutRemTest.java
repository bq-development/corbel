package com.bq.oss.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.bq.oss.corbel.resources.rem.dao.NotFoundException;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ResmiPutRemTest extends ResmiRemTest {

    private static final ResourceId OTHER_ID = new ResourceId("otherId");
    private static final String TEST_URI = "testUri/123";
    private static final String TEST_RELATION = "relation";
    private static final String TEST_INVALID_URI = "testUri/asdf/asdf";
    private AbstractResmiRem putRem;

    @Override
    @Before
    public void setup() {
        super.setup();
        putRem = new ResmiPutRem(resmiServiceMock);
    }

    private RequestParameters<ResourceParameters> getResourceParametersMockWithCondition(Optional<List<ResourceQuery>> conditions) {
        ResourceParameters resourceParametersMock = mock(ResourceParameters.class);
        RequestParameters<ResourceParameters> requestParametersMock = mock(RequestParameters.class);
        when(requestParametersMock.getApiParameters()).thenReturn(resourceParametersMock);
        when(resourceParametersMock.getConditions()).thenReturn(conditions);
        return requestParametersMock;

    }

    @Test
    public void updateResourceTest() {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));

        RequestParameters<ResourceParameters> requestParametersMock = getResourceParametersMockWithCondition(Optional.empty());
        Response response = putRem.resource(TEST_TYPE, TEST_ID, requestParametersMock, Optional.of(json));
        assertThat(response.getStatus()).isEqualTo(204);

        response = putRem.resource(TEST_TYPE, OTHER_ID, requestParametersMock, Optional.of(json));
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void updateResourceTestWithCondition() throws StartsWithUnderscoreException {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        List<ResourceQuery> resourceQueryListMock = mock(List.class);
        RequestParameters<ResourceParameters> requestParametersMock = getResourceParametersMockWithCondition(Optional
                .of(resourceQueryListMock));

        when(resmiServiceMock.conditionalUpdate(TEST_TYPE, TEST_ID.getId(), json, resourceQueryListMock)).thenReturn(json);
        Response response = putRem.resource(TEST_TYPE, TEST_ID, requestParametersMock, Optional.of(json));
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void updateResourceTestWithFailCondition() throws StartsWithUnderscoreException {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        List<ResourceQuery> resourceQueryListMock = mock(List.class);
        RequestParameters<ResourceParameters> requestParametersMock = getResourceParametersMockWithCondition(Optional
                .of(resourceQueryListMock));

        when(resmiServiceMock.conditionalUpdate(TEST_TYPE, TEST_ID.getId(), json, resourceQueryListMock)).thenReturn(null);
        Response response = putRem.resource(TEST_TYPE, TEST_ID, requestParametersMock, Optional.of(json));
        assertThat(response.getStatus()).isEqualTo(412);
    }

    @Test
    public void updateResourceWithUnderscoreInAttributeNameTest() throws StartsWithUnderscoreException {
        JsonObject json = new JsonObject();
        json.add("a", new JsonPrimitive("1"));
        json.add("_b", new JsonPrimitive("2"));

        doThrow(new StartsWithUnderscoreException("_b")).when(resmiServiceMock).upsert(any(), any(), eq(json));

        RequestParameters<ResourceParameters> requestParametersMock = getResourceParametersMockWithCondition(Optional.empty());

        Response response = putRem.resource(TEST_TYPE, TEST_ID, requestParametersMock, Optional.of(json));
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void updateMissingTest() {
        Response response = putRem.resource(TEST_TYPE, TEST_ID, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testPutCollectionNotAllowed() {
        Response response = putRem.collection(TEST_TYPE, null, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testPutRelation() {
        Response response = putRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParameters(TEST_URI), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testPutRelationWithUnderscoreInAttributeNameTest() throws NotFoundException, StartsWithUnderscoreException {
        JsonObject json = new JsonObject();
        json.add("_b", new JsonPrimitive("2"));

        doThrow(new StartsWithUnderscoreException("_b")).when(resmiServiceMock).createRelation(any(), any(), any(), any(), eq(json));

        Response response = putRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParameters(TEST_URI), Optional.ofNullable(json));
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testPutRelationWithData() {
        Response response = putRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParameters(TEST_URI), Optional.of(getTestRelationData()));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testInvalidUriPutRelation() {
        Response response = putRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParameters(TEST_INVALID_URI), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testNullUriPutRelation() {
        Response response = putRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParametersWithEmptyUri(), Optional.of(getTestResource()));
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
