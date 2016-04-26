package io.corbel.resources.rem.acl;

import static io.corbel.resources.rem.acl.AclTestUtils.getEntityWithoutAcl;
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

import com.google.gson.JsonObject;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.RemService;

/**
 * @author Cristian del Cerro
 */

@RunWith(MockitoJUnitRunner.class) public class AclGetRemTest {

    private static final String USER_ID = "userId";
    private static final String GROUP_ID = "groupId";
    private static final String TYPE = "type";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    private static final String REQUESTED_DOMAIN_ID = "requestedDomainId";

    private AclGetRem rem;

    @Mock private AclResourcesService aclResourcesService;
    @Mock private List<MediaType> acceptedMediaTypes;
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> resourceParameters;
    @Mock private RequestParameters<CollectionParameters> collectionParameters;
    @Mock private RequestParameters<RelationParameters> relationParameters;
    @Mock private TokenInfo tokenInfo;
    @Mock private Response getResponse;

    @Before
    public void setUp() throws Exception {

        when(getResponse.getStatus()).thenReturn(200);
        when(aclResourcesService.getResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(getResponse);
        rem = new AclGetRem(aclResourcesService);
        rem.setRemService(remService);

        when(tokenInfo.getUserId()).thenReturn(USER_ID);
        when(tokenInfo.getGroups()).thenReturn(Collections.singletonList(GROUP_ID));
        when(resourceParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(collectionParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(relationParameters.getTokenInfo()).thenReturn(tokenInfo);

        when(collectionParameters.getRequestedDomain()).thenReturn(REQUESTED_DOMAIN_ID);
        when(resourceParameters.getRequestedDomain()).thenReturn(REQUESTED_DOMAIN_ID);
        when(relationParameters.getRequestedDomain()).thenReturn(REQUESTED_DOMAIN_ID);
    }

    @Test
    public void testGetResourceNoUserId() throws AclFieldNotPresentException {
        when(tokenInfo.getUserId()).thenReturn(null);
        when(resourceParameters.getTokenInfo()).thenReturn(tokenInfo);

        JsonObject entity = getEntityWithoutAcl();
        JsonObject acl = new JsonObject();
        acl.addProperty("ALL", "READ");
        entity.add("_acl", acl);

        when(aclResourcesService.getResourceIfIsAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.READ)))
                .thenReturn(Optional.of(entity));
        Response response = rem.resource(TYPE, RESOURCE_ID, resourceParameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test(expected = WebApplicationException.class)
    public void testGetResourceNotFoundObject() throws AclFieldNotPresentException {
        when(getResponse.getStatus()).thenReturn(404);
        when(getResponse.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        doThrow(new WebApplicationException(getResponse)).when(aclResourcesService).getResourceIfIsAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE),
                eq(RESOURCE_ID), eq(AclPermission.READ));
        try {
            rem.resource(TYPE, RESOURCE_ID, resourceParameters, null, Optional.empty());
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(404);
            throw wae;
        }
    }

    @Test
    public void testGetResourceGetEntity() throws AclFieldNotPresentException {
        JsonObject entity = getEntityWithoutAcl();
        JsonObject acl = new JsonObject();
        acl.addProperty(USER_ID, "ADMIN");
        entity.add("_acl", acl);

        when(aclResourcesService.getResourceIfIsAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.READ)))
                .thenReturn(Optional.of(entity));
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.resource(TYPE, RESOURCE_ID, resourceParameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(entity);
    }

    @Test
    public void testGetCollectionNoUserId() {
        JsonObject entity = getEntityWithoutAcl();
        JsonObject acl = new JsonObject();
        acl.addProperty("ALL", "READ");
        entity.add("_acl", acl);

        CollectionParameters apiParameters = mock(CollectionParameters.class);
        when(collectionParameters.getAcceptedMediaTypes()).thenReturn(Collections.singletonList(MediaType.APPLICATION_JSON));
        when(collectionParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getQueries()).thenReturn(Optional.empty());
        when(tokenInfo.getUserId()).thenReturn(null);
        when(collectionParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(collectionParameters.getAcceptedMediaTypes()).thenReturn(Collections.singletonList(MediaType.APPLICATION_JSON));
        when(aclResourcesService.getCollection(any(), eq(TYPE), eq(collectionParameters), any())).thenReturn(getResponse);
        when(getResponse.getEntity()).thenReturn(entity);

        Response response = rem.collection(TYPE, collectionParameters, null, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testGetCollectionNoJsonMediaType() {
        Response response = rem.collection(TYPE, collectionParameters, null, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testGetCollection() {
        JsonObject entity = getEntityWithoutAcl();
        JsonObject acl = new JsonObject();
        acl.addProperty(USER_ID, "ADMIN");
        entity.add("_acl", acl);

        CollectionParameters apiParameters = mock(CollectionParameters.class);
        when(collectionParameters.getAcceptedMediaTypes()).thenReturn(Collections.singletonList(MediaType.APPLICATION_JSON));
        when(collectionParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getQueries()).thenReturn(Optional.empty());

        when(aclResourcesService.getCollection(any(), eq(TYPE), eq(collectionParameters), any())).thenReturn(getResponse);
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.collection(TYPE, collectionParameters, null, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(entity);
    }

    @Test
    public void testGetCollectionWithPermissionForAllUsers() {
        JsonObject entity = getEntityWithoutAcl();
        JsonObject acl = new JsonObject();
        acl.addProperty("ALL", "READ");
        entity.add("_acl", acl);

        CollectionParameters apiParameters = mock(CollectionParameters.class);
        when(collectionParameters.getAcceptedMediaTypes()).thenReturn(Collections.singletonList(MediaType.APPLICATION_JSON));
        when(collectionParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getQueries()).thenReturn(Optional.empty());

        when(aclResourcesService.getCollection(any(), eq(TYPE), eq(collectionParameters), any())).thenReturn(getResponse);
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.collection(TYPE, collectionParameters, null, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(entity);
    }

    @Test
    public void testGetRelationWithWildcardInOrigin() {
        ResourceId resourceId = new ResourceId("_");

        RelationParameters apiParameters = mock(RelationParameters.class);
        when(relationParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getPredicateResource()).thenReturn(Optional.of("idDst"));

        Response response = rem.relation(TYPE, resourceId, TYPE, relationParameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testGetRelation() throws AclFieldNotPresentException {
        JsonObject entity = getEntityWithoutAcl();
        JsonObject acl = new JsonObject();
        acl.addProperty(USER_ID, "ADMIN");
        entity.add("_acl", acl);

        ResourceId resourceId = new ResourceId("idOrigin");

        when(aclResourcesService.isAuthorized(eq(REQUESTED_DOMAIN_ID), eq(tokenInfo), eq(TYPE), eq(resourceId), eq(AclPermission.READ))).thenReturn(true);

        RelationParameters apiParameters = mock(RelationParameters.class);
        when(relationParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getPredicateResource()).thenReturn(Optional.of("idDist"));

        when(aclResourcesService.getRelation(any(), eq(TYPE), eq(resourceId), eq(TYPE), eq(relationParameters), any())).thenReturn(getResponse);
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.relation(TYPE, resourceId, TYPE, relationParameters, Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
    }

}
