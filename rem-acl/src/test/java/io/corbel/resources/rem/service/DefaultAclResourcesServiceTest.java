package io.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonParser;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.service.RemService;

/**
 * @author Rubén Carrasco
 *
 */
@RunWith(MockitoJUnitRunner.class) @SuppressWarnings("unchecked") public class DefaultAclResourcesServiceTest {

    private static final String ALL = "ALL";
    private static final ResourceId ID_NOT_ALLOWED = new ResourceId("idNotAllowed");
    private static final ResourceId RESOURCE_ID = new ResourceId("idAllowed");
    private static final String USER_ID = "userId";
    private static final String GROUP_ID = "groupId";
    private static final Collection<String> GROUPS = Collections.singletonList(GROUP_ID);
    private static final String TYPE = "type";

    private final DefaultAclResourcesService aclService = new DefaultAclResourcesService();
    @Mock private RemService remService;
    @Mock private Rem resmiRem;
    JsonParser parser = new JsonParser();

    @Before
    public void setUp() throws Exception {
        when(remService.getRem(any(), any(), any())).thenReturn(resmiRem);
        aclService.setRemService(remService);
    }

    @Test
    public void testAllowedWithUserId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testAllowedWithAll() {
        Response response = mockResponseWithAcl(ALL);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testNotAllowedOperationWithUserId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedOperationWithAll() {
        Response response = mockResponseWithAcl(ALL);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedWithUserId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + "asdf");
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowed() {
        Response response = mockResponse();
        when(resmiRem.resource(any(), eq(ID_NOT_ALLOWED), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, ID_NOT_ALLOWED, AclPermission.READ)).isFalse();
    }

    @Test
    public void testAllowedWithGroupId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.GROUP_PREFIX + GROUP_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testNotAllowedWithGroupId() {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.GROUP_PREFIX + GROUP_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedWithBadAcl() {
        Response response = mockResponseWithBadAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        when(resmiRem.resource(any(), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(USER_ID, GROUPS, TYPE, RESOURCE_ID, AclPermission.READ)).isFalse();
    }

    private Response mockResponseWithAcl(String scope) {
        return mockResponse(
                "{ \"_acl\": { \"" + scope + "\": { \"permission\": \"READ\", \"properties\": {\"email\": \"asdf@funkifake.com\"} } } }");
    }

    private Response mockResponseWithBadAcl(String scope) {
        return mockResponse("{ \"_acl\": { \"" + scope + "\": { \"permission\": {}, \"properties\": {} } } }");
    }

    private Response mockResponse() {
        return mockResponse("{}");
    }

    private Response mockResponse(String json) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.getEntity()).thenReturn(parser.parse(json));
        return response;
    }

}
