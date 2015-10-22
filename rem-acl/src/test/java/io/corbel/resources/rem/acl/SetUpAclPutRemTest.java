package io.corbel.resources.rem.acl;

import static io.corbel.resources.rem.acl.AclTestUtils.getEntity;
import static io.corbel.resources.rem.acl.AclTestUtils.getEntityWithoutAcl;
import static io.corbel.resources.rem.utils.AclUtils.buildMessage;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.service.RemService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import com.google.gson.JsonObject;

/**
 * @author Cristian del Cerro
 */
@RunWith(MockitoJUnitRunner.class) public class SetUpAclPutRemTest {

    private static final String USER_ID = "userId";
    private static final String GROUP_ID = "groupId";
    private static final List<String> GROUPS = Collections.singletonList(GROUP_ID);
    private static final String ALL = "ALL";
    private static final String TYPE = "type";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");

    private SetUpAclPutRem rem;

    @Mock private AclGetRem getRem;
    @Mock private AclResourcesService aclResourcesService;
    @Mock private List<MediaType> acceptedMediaTypes;
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> parameters;
    @Mock private TokenInfo tokenInfo;
    @Mock private Response getResponse;

    @Before
    public void setUp() throws Exception {

        when(getResponse.getStatus()).thenReturn(200);
        when(aclResourcesService.getResource(any(), eq(TYPE), eq(RESOURCE_ID), any())).thenReturn(getResponse);
        rem = new SetUpAclPutRem(aclResourcesService, Collections.singletonList(getRem));
        rem.setRemService(remService);

        when(tokenInfo.getUserId()).thenReturn(USER_ID);
        when(tokenInfo.getGroups()).thenReturn(GROUPS);
        when(parameters.getTokenInfo()).thenReturn(tokenInfo);
    }

    @Test
    public void testNoUserId() {
        when(tokenInfo.getUserId()).thenReturn(null);
        when(parameters.getTokenInfo()).thenReturn(tokenInfo);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testEmptyObject() throws IOException {
        InputStream entity = mock(InputStream.class);
        when(entity.available()).thenReturn(0);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.of(entity));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testUserNotInAcl() {
        JsonObject entity = getEntity("asdf", "");
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(new ByteArrayInputStream(getEntityWithoutAcl().toString().getBytes())));
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).isEqualTo(buildMessage(AclPermission.ADMIN));
    }

    @Test
    public void testReadPermission() {
        JsonObject entity = getEntity(USER_ID, AclPermission.READ.toString());
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(new ByteArrayInputStream(getEntityWithoutAcl().toString().getBytes())));
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).contains(AclPermission.ADMIN.toString());
    }

    @Test
    public void testWritePermission() {
        JsonObject entity = getEntity(USER_ID, AclPermission.WRITE.toString());
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(new ByteArrayInputStream(getEntityWithoutAcl().toString().getBytes())));
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).contains(AclPermission.ADMIN.toString());
    }

    @Test
    public void testUpdateAclPermission() {
        List<String> ids = Arrays.asList("user:" + USER_ID, "group:" + GROUP_ID, ALL);

        for (String id : ids) {
            when(aclResourcesService.isAuthorized(eq(USER_ID), eq(GROUPS), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(
                    true);

            Response beforeResponse = mock(Response.class);
            when(beforeResponse.getStatus()).thenReturn(200);

            JsonObject aclValue = new JsonObject();
            aclValue.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.ADMIN.toString());
            aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

            JsonObject objectToSave = new JsonObject();
            objectToSave.add(id, aclValue);

            JsonObject acl = new JsonObject();
            acl.add(DefaultAclResourcesService._ACL, objectToSave);

            when(beforeResponse.getStatus()).thenReturn(200);
            when(aclResourcesService.updateResource(any(), eq(TYPE), eq(RESOURCE_ID), eq(parameters), eq(acl))).thenReturn(beforeResponse);

            Response afterResponse = rem.resource(TYPE, RESOURCE_ID, parameters,
                    Optional.of(new ByteArrayInputStream(objectToSave.toString().getBytes())));
            assertThat(afterResponse.getStatus()).isEqualTo(200);
        }
    }

    @Test
    public void testUpdateAclPermissionOverALLUsersAsNotAdmin() {
        when(aclResourcesService.isAuthorized(eq(USER_ID), eq(GROUPS), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN)))
                .thenReturn(true);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);

        JsonObject aclValue = new JsonObject();
        aclValue.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.WRITE.toString());
        aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        JsonObject objectToSave = new JsonObject();
        objectToSave.add("ALL", aclValue);

        JsonObject acl = new JsonObject();
        acl.add(DefaultAclResourcesService._ACL, objectToSave);

        when(response.getStatus()).thenReturn(400);
        when(aclResourcesService.updateResource(any(), eq(TYPE), eq(RESOURCE_ID), eq(parameters), eq(acl))).thenReturn(response);

        response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.of(new ByteArrayInputStream(objectToSave.toString().getBytes())));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testUpdateBadAclPermission() {
        List<String> ids = Arrays.asList("user:" + USER_ID, "group:" + GROUP_ID);

        for (String id : ids) {
            when(aclResourcesService.isAuthorized(eq(USER_ID), eq(GROUPS), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(
                    true);

            JsonObject aclValue = new JsonObject();
            aclValue.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.READ.toString());
            aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

            JsonObject objectToSave = new JsonObject();
            objectToSave.addProperty(id, AclPermission.READ.toString());

            Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                    Optional.of(new ByteArrayInputStream(objectToSave.toString().getBytes())));
            assertThat(response.getStatus()).isEqualTo(400);
        }
    }

    private io.corbel.lib.ws.model.Error getError(Response response) {
        return (Error) response.getEntity();
    }

}
