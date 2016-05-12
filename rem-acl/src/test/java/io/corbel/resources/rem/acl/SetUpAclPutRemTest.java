package io.corbel.resources.rem.acl;

import static io.corbel.resources.rem.acl.AclTestUtils.getEntity;
import static io.corbel.resources.rem.utils.AclUtils.buildMessage;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.google.gson.JsonPrimitive;

import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import com.google.gson.JsonObject;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.service.RemService;

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
    private static final String REQUESTED_DOMAIN_ID = "requestedDomainId";

    private SetUpAclPutRem rem;

    @Mock private AclResourcesService aclResourcesService;
    @Mock private List<MediaType> acceptedMediaTypes;
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> parameters;
    @Mock private RequestParameters<ResourceParameters> emptyParameters;
    @Mock private TokenInfo tokenInfo;
    @Mock private Response getResponse;

    @Before
    public void setUp() throws Exception {
        when(getResponse.getStatus()).thenReturn(200);
        when(aclResourcesService.getResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(getResponse);
        rem = new SetUpAclPutRem(aclResourcesService, Collections.emptyList());
        rem.setRemService(remService);

        when(tokenInfo.getUserId()).thenReturn(USER_ID);
        when(tokenInfo.getGroups()).thenReturn(GROUPS);

        when(emptyParameters.getTokenInfo()).thenReturn(tokenInfo);

        when(parameters.getTokenInfo()).thenReturn(tokenInfo);

        when(parameters.getOptionalApiParameters()).thenReturn(Optional.empty());
        when(parameters.getParams()).thenReturn(new MultivaluedHashMap<>());
        MultivaluedMap<String, String> headers = new MultivaluedStringMap();
        headers.putSingle("Content-Length", "10");
        when(parameters.getHeaders()).thenReturn(headers);

        when(parameters.getRequestedDomain()).thenReturn(REQUESTED_DOMAIN_ID);

    }

    @Test
    public void testNoUserId() {
        when(tokenInfo.getUserId()).thenReturn(null);
        when(parameters.getTokenInfo()).thenReturn(tokenInfo);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testEmptyObject() throws IOException {
        InputStream entity = mock(InputStream.class);
        when(entity.available()).thenReturn(0);
        Response response = rem.resource(TYPE, RESOURCE_ID, emptyParameters, Optional.of(entity), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testUserNotInAcl() {
        JsonObject entity = getEntity("asdf", "");
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(getEntityWithoutAcl()), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).isEqualTo(buildMessage(AclPermission.ADMIN));
    }

    @Test
    public void testReadPermission() {
        JsonObject entity = getEntity(USER_ID, AclPermission.READ.toString());
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(getEntityWithoutAcl()), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).contains(AclPermission.ADMIN.toString());
    }

    @Test
    public void testWritePermission() {
        JsonObject entity = getEntity(USER_ID, AclPermission.WRITE.toString());
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(getEntityWithoutAcl()), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).contains(AclPermission.ADMIN.toString());
    }

    @Test
    public void testUpdateAclPermission() throws AclFieldNotPresentException {
        List<String> ids = Arrays.asList("user:" + USER_ID, "group:" + GROUP_ID, ALL);

        for (String id : ids) {
            when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(true);

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
            when(aclResourcesService.updateResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), eq(acl), any())).thenReturn(beforeResponse);

            Response afterResponse = rem.resource(TYPE, RESOURCE_ID, parameters,
                    Optional.of(new ByteArrayInputStream(objectToSave.toString().getBytes())), Optional.empty());
            assertThat(afterResponse.getStatus()).isEqualTo(200);
        }
    }

    @Test
    public void testUpdateAclPermissionOverALLUsersAsNotAdmin() throws AclFieldNotPresentException {
        when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(true);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);

        JsonObject aclValue = new JsonObject();
        aclValue.addProperty(DefaultAclResourcesService.PERMISSION, AclPermission.WRITE.toString());
        aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        JsonObject objectToSave = new JsonObject();
        objectToSave.add("ALL", aclValue);

        JsonObject permission = new JsonObject();
        permission.addProperty("permission",  AclPermission.ADMIN.toString());
        permission.add("properties",  new JsonObject());

        objectToSave.add("user:" + USER_ID, permission);

        JsonObject acl = new JsonObject();
        acl.add(DefaultAclResourcesService._ACL, objectToSave);

        when(response.getStatus()).thenReturn(200);
        when(aclResourcesService.updateResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), eq(acl), any())).thenReturn(response);

        response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.of(getEntityAsInputStream(objectToSave)));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testUpdateBadAclPermission() throws AclFieldNotPresentException {
        List<String> ids = Arrays.asList("user:" + USER_ID, "group:" + GROUP_ID);

        for (String id : ids) {
            when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(true);

            JsonObject objectToSave = new JsonObject();
            objectToSave.addProperty(id, AclPermission.READ.toString());

            Response expectedResponse = mock(Response.class);
            when(expectedResponse.getStatus()).thenReturn(200);
            when(aclResourcesService.updateResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), any(), any())).thenReturn(expectedResponse);

            Response response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.of(new ByteArrayInputStream(objectToSave.toString().getBytes())));
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    private InputStream getEntityWithoutAcl () {
        return getEntityAsInputStream(AclTestUtils.getEntityWithoutAcl());
    }

    private InputStream getEntityAsInputStream (JsonObject jsonObject) {
        return new ByteArrayInputStream(jsonObject.toString().getBytes());
    }

    private io.corbel.lib.ws.model.Error getError(Response response) {
        return (Error) response.getEntity();
    }

}
