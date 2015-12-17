package io.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.RequestParametersImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.AclPermission;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.ResourceId;

/**
 * @author Rubén Carrasco
 */
@RunWith(MockitoJUnitRunner.class) @SuppressWarnings("unchecked") public class DefaultAclResourcesServiceTest {

    private static final String ALL = "ALL";
    private static final ResourceId ID_NOT_ALLOWED = new ResourceId("idNotAllowed");
    private static final ResourceId RESOURCE_ID = new ResourceId("idAllowed");
    private static final String USER_ID = "userId";
    private static final String GROUP_ID = "groupId";
    private static final Collection<String> GROUPS = Collections.singletonList(GROUP_ID);
    private static final String REQUESTED_DOMAIN_ID = "requestedDomainId";
    private static final String DOMAIN_ID = "domainId";
    private static final String TYPE = "type";
    private static final String ADMINS_COLLECTION = "adminsCollection";
    private static final char JOINER = ':';
    private static final String MANAGED_COLLECTION_ID = REQUESTED_DOMAIN_ID + JOINER + TYPE;
    private static final ResourceId MANAGED_COLLECTION_RESOURCE = new ResourceId(MANAGED_COLLECTION_ID);
    private static final ResourceId MANAGED_DOMAIN_RESOURCE = new ResourceId(REQUESTED_DOMAIN_ID);

    @Mock private TokenInfo tokenInfoMock;
    @Mock private RemService remService;
    @Mock private Rem resmiGetRem;
    @Mock private Rem resmiPutRem;
    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    private DefaultAclResourcesService aclService = new DefaultAclResourcesService(gson, ADMINS_COLLECTION);

     @Before
    public void setUp() throws Exception {
        when(remService.getRem(DefaultAclResourcesService.RESMI_GET)).thenReturn(resmiGetRem);
        when(remService.getRem(DefaultAclResourcesService.RESMI_PUT)).thenReturn(resmiPutRem);
        aclService.setRemService(remService);
        when(tokenInfoMock.getUserId()).thenReturn(USER_ID);
        when(tokenInfoMock.getGroups()).thenReturn(GROUPS);
        when(tokenInfoMock.getDomainId()).thenReturn(DOMAIN_ID);
    }

    @Test
    public void testAllowedWithUserId() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testAllowedWithAll() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(ALL);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID,tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test(expected = AclFieldNotPresentException.class)
    public void testNotAllowedWithoutAclObject() throws AclFieldNotPresentException {
        Response response = mockResponse();
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.READ);
    }

    @Test
    public void testNotAllowedOperationWithUserId() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedOperationWithAll() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(ALL);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedWithUserId() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.USER_PREFIX + "asdf");
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowed() throws AclFieldNotPresentException {
        Response response = mockResponseWithEmptyAcl();
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(ID_NOT_ALLOWED), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, ID_NOT_ALLOWED, AclPermission.READ)).isFalse();
    }

    @Test
    public void testAllowedWithGroupId() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.GROUP_PREFIX + GROUP_ID);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.READ)).isTrue();
    }

    @Test
    public void testNotAllowedWithGroupId() throws AclFieldNotPresentException {
        Response response = mockResponseWithAcl(DefaultAclResourcesService.GROUP_PREFIX + GROUP_ID);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.WRITE)).isFalse();
    }

    @Test
    public void testNotAllowedWithBadAcl() throws AclFieldNotPresentException {
        Response response = mockResponseWithBadAcl(DefaultAclResourcesService.USER_PREFIX + USER_ID);
        Response managedResponse = mockNotFoundResponse();
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), any(), any(), eq(Optional.empty()))).thenReturn(managedResponse);
        when(resmiGetRem.resource(eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);
        assertThat(aclService.isAuthorized(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE, RESOURCE_ID, AclPermission.READ)).isFalse();
    }

    private Response mockResponseWithEmptyAcl() {
        return mockResponse("{ \"_acl\": {} }");
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

    private Response mockNotFoundResponse() {
        Response response = mock(Response.class);
        Response.StatusType statusInfo = Response.Status.INTERNAL_SERVER_ERROR;
        when(response.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());
        when(response.getStatusInfo()).thenReturn(statusInfo);
        return response;
    }

    @Test(expected = WebApplicationException.class)
    public void testFailedManagedCollection() {
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        when(responseMock.getStatusInfo()).thenReturn(mock(Response.StatusType.class));

        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);

        try {
            assertThat(aclService.isManagedBy(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE)).isTrue();
        } catch (WebApplicationException e) {
            verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
            verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
            verifyNoMoreInteractions(remService, resmiGetRem);
            throw e;
        }
    }

    @Test
    public void testManagedCollection() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.singletonList(USER_ID),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);

        assertThat(aclService.isManagedBy(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiGetRem);
    }

    @Test
    public void testManagedCollectionByGroup() throws IOException {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.singletonList(GROUP_ID));
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);

        assertThat(aclService.isManagedBy(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiGetRem);
    }

    @Test
    public void testManagedCollectionNotByUser() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        Response domainResponseMock = mock(Response.class);
        when(domainResponseMock.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());
        when(domainResponseMock.getStatusInfo()).thenReturn(mock(Response.StatusType.class));


        RequestParameters requestParameters = new RequestParametersImpl<>(null, null, "_silkroad", null, null, null, null);
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), eq(requestParameters), any())).thenReturn(responseMock);
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), eq(requestParameters), any())).thenReturn(domainResponseMock);

        assertThat(aclService.isManagedBy(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE)).isFalse();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), eq(requestParameters), any());
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), eq(requestParameters), any());
        verifyNoMoreInteractions(remService, resmiGetRem);
    }

    @Test
    public void testManagedCollectionByDomainAdmin() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        ManagedCollection domainManagedCollection = new ManagedCollection(DOMAIN_ID, Collections.singletonList(USER_ID),
                Collections.emptyList());
        Response domainResponseMock = mock(Response.class);
        when(domainResponseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(domainResponseMock.getEntity()).thenReturn(gson.toJsonTree(domainManagedCollection));

        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any())).thenReturn(domainResponseMock);

        assertThat(aclService.isManagedBy(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiGetRem);
    }

    @Test
    public void testManagedCollectionByGroupDomainAdmin() {
        ManagedCollection managedCollection = new ManagedCollection(MANAGED_COLLECTION_ID, Collections.emptyList(),
                Collections.emptyList());
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(responseMock.getEntity()).thenReturn(gson.toJsonTree(managedCollection));

        ManagedCollection domainManagedCollection = new ManagedCollection(DOMAIN_ID, Collections.emptyList(),
                Collections.singletonList(GROUP_ID));
        Response domainResponseMock = mock(Response.class);
        when(domainResponseMock.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(domainResponseMock.getEntity()).thenReturn(gson.toJsonTree(domainManagedCollection));

        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any())).thenReturn(responseMock);
        when(resmiGetRem.resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any())).thenReturn(domainResponseMock);

        assertThat(aclService.isManagedBy(REQUESTED_DOMAIN_ID, tokenInfoMock, TYPE)).isTrue();

        verify(remService).getRem(DefaultAclResourcesService.RESMI_GET);
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_COLLECTION_RESOURCE), any(), any());
        verify(resmiGetRem).resource(eq(ADMINS_COLLECTION), eq(MANAGED_DOMAIN_RESOURCE), any(), any());
        verifyNoMoreInteractions(remService, resmiGetRem);
    }

}
