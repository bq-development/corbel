package io.corbel.resources.rem.acl;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.RemService;

/**
 * @author Rubén Carrasco
 *
 */
@RunWith(MockitoJUnitRunner.class) public class AclDeleteRemTest {

    private static final String USER_ID = "userId";
    private static final Optional<String> OPT_USER_ID = Optional.of(USER_ID);
    private static final String GROUP_ID = "groupId";
    private static final String TYPE = "type";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    private static final String REQUESTED_DOMAIN_ID = "requestedDomainId";

    private AclDeleteRem rem;

    @Mock private AclGetRem getRem;
    @Mock private AclResourcesService aclResourcesService;
    @Mock private List<MediaType> acceptedMediaTypes;
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> resourceParameters;
    @Mock private RequestParameters<RelationParameters> relationParameters;
    @Mock private TokenInfo tokenInfo;
    @Mock private Response getResponse;

    @Before
    public void setUp() throws Exception {

        when(getResponse.getStatus()).thenReturn(200);
        when(aclResourcesService.getResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(getResponse);
        rem = new AclDeleteRem(aclResourcesService, Collections.singletonList(getRem));
        rem.setRemService(remService);

        when(tokenInfo.getUserId()).thenReturn(USER_ID);
        when(tokenInfo.getGroups()).thenReturn(Collections.singletonList(GROUP_ID));
        when(resourceParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(relationParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(resourceParameters.getRequestedDomain()).thenReturn(REQUESTED_DOMAIN_ID);
        when(relationParameters.getRequestedDomain()).thenReturn(REQUESTED_DOMAIN_ID);
    }

    @Test
    public void testDeleteResourceNoUserId() {
        when(tokenInfo.getUserId()).thenReturn(null);
        when(resourceParameters.getTokenInfo()).thenReturn(tokenInfo);
        Response response = rem.resource(TYPE, RESOURCE_ID, resourceParameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test(expected = WebApplicationException.class)
    public void testDeleteResourceNotFoundObject() throws AclFieldNotPresentException {
        when(getResponse.getStatus()).thenReturn(404);
        when(getResponse.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        doThrow(new WebApplicationException(getResponse)).when(aclResourcesService).isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID),
                eq(AclPermission.ADMIN));
        try {
            rem.resource(TYPE, RESOURCE_ID, resourceParameters, null, Optional.empty());
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(404);
            throw wae;
        }
    }

    @Test
    public void testDeleteResourceNotPermission() throws AclFieldNotPresentException {
        when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(false);
        Response response = rem.resource(TYPE, RESOURCE_ID, resourceParameters, null);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(getError(response).getErrorDescription()).contains(AclPermission.ADMIN.toString());
    }

    @Test
    public void testDeleteResourceWithCorrectPermissions() throws AclFieldNotPresentException {
        when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(true);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(204);
        when(aclResourcesService.deleteResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(response);

        response = rem.resource(TYPE, RESOURCE_ID, resourceParameters, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testDeleteRelationWithCorrectPermissions() throws AclFieldNotPresentException {
        when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(true);
        RelationParameters apiParameters = mock(RelationParameters.class);
        when(relationParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getPredicateResource()).thenReturn(Optional.of("idDist"));

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(204);
        when(aclResourcesService.deleteRelation(any(), eq(TYPE), eq(RESOURCE_ID), eq(TYPE), any(), any())).thenReturn(response);

        response = rem.relation(TYPE, RESOURCE_ID, TYPE, relationParameters, null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testRelationWildcardInOrigin() throws AclFieldNotPresentException {
        when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.ADMIN))).thenReturn(true);
        RelationParameters apiParameters = mock(RelationParameters.class);
        when(relationParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getPredicateResource()).thenReturn(Optional.empty());
        ResourceId resourceId = new ResourceId("_");

        Response response = rem.relation(TYPE, resourceId, TYPE, relationParameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    private Error getError(Response response) {
        return (Error) response.getEntity();
    }

}
