package com.bq.oss.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.google.gson.JsonObject;

/**
 * @author Cristian del Cerro
 */
public class ResmiDeleteRemTest extends ResmiRemTest {

    private AbstractResmiRem deleteRem;

    private final String TEST_RELATION = "relationtest";
    private static final String TEST_URI = "testUri/123";

    @Override
    @Before
    public void setup() {
        super.setup();
        deleteRem = new ResmiDeleteRem(resmiServiceMock);
    }

    @Test
    public void testDeleteOkResource() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("field1", "field1content");

        Response response = deleteRem.resource(TEST_TYPE, TEST_ID, null, Optional.of(jsonObject));
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testDeleteOkRelation() {
        Response response = deleteRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParameters(TEST_URI), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testDeleteOkRelationWithWildCardId() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("field1", "field1content");
        Response response = deleteRem
                .relation(TEST_TYPE, TEST_WILDCARD_ID, TEST_RELATION, getParameters(TEST_URI), Optional.of(jsonObject));
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testMethodDeleteRelationFromSrcID() {
        Response response = deleteRem.relation(TEST_TYPE, TEST_ID, TEST_RELATION, getParametersWithEmptyUri(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(204);
        verify(resmiServiceMock).deleteRelation(eq(TEST_TYPE), eq(TEST_ID), eq(TEST_RELATION), eq(Optional.empty()));
    }

    @Test
    public void testDeleteRelationNotAllowed() {
        ResourceId resourceId = new ResourceId((ResourceId.WILDCARD_RESOURCE_ID));
        Optional dstId = Optional.empty();
        Response response = deleteRem.relation(TEST_TYPE, resourceId, TEST_RELATION, getParametersWithEmptyUri(), dstId);
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testDeleteCollectionNotAllowed() {
        Response response = deleteRem.collection(TEST_TYPE, null, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

}
