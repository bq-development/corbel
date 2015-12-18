package io.corbel.resources.rem.acl;

import static io.corbel.resources.rem.acl.AclTestUtils.getEntity;
import static io.corbel.resources.rem.acl.AclTestUtils.getEntityWithoutAcl;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.acl.exception.AclFieldNotPresentException;
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
@RunWith(MockitoJUnitRunner.class) public class AclPutRemTest {

    private static final String USER_ID = "userId";
    private static final Optional<String> OPT_USER_ID = Optional.of(USER_ID);
    private static final String GROUP_ID = "groupId";
    private static final String TYPE = "type";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    private static final String ACL_CONFIGURATION_COLLECTION = "acl:Configuration";

    private AclPutRem rem;

    @Mock private AclGetRem getRem;
    @Mock private AclResourcesService aclResourcesService;
    @Mock private List<MediaType> acceptedMediaTypes;
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> parameters;
    @Mock private TokenInfo tokenInfo;
    @Mock private Response getResponse;
    @Mock private RequestParameters<RelationParameters> relationParameters;

    @Before
    public void setUp() throws Exception {

        when(getResponse.getStatus()).thenReturn(200);
        when(aclResourcesService.getResource(any(), eq(TYPE), eq(RESOURCE_ID), any())).thenReturn(getResponse);
        rem = new AclPutRem(aclResourcesService, Collections.singletonList(getRem));
        rem.setRemService(remService);

        when(tokenInfo.getUserId()).thenReturn(USER_ID);
        when(tokenInfo.getGroups()).thenReturn(Collections.singletonList(GROUP_ID));
        when(parameters.getTokenInfo()).thenReturn(tokenInfo);
        when(relationParameters.getTokenInfo()).thenReturn(tokenInfo);
    }

    @Test
    public void testPutResourceEmptyObject() throws IOException {
        InputStream entity = mock(InputStream.class);
        when(entity.available()).thenReturn(0);
        Response response = rem.resource(TYPE, RESOURCE_ID, parameters, Optional.of(entity));
        assertThat(response.getStatus()).isEqualTo(400);
    }


    @Test
    public void testUpdateResourceObject() throws AclFieldNotPresentException {
        JsonObject entity = getEntity(USER_ID, AclPermission.ADMIN.toString());
        when(getResponse.getEntity()).thenReturn(entity);

        when(aclResourcesService.getResourceIfIsAuthorized(eq(tokenInfo), eq(TYPE), eq(RESOURCE_ID), eq(AclPermission.WRITE)))
                .thenReturn(Optional.of(entity));

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(aclResourcesService.updateResource(any(), eq(TYPE), eq(RESOURCE_ID), eq(parameters), eq(getEntityWithoutAcl())))
                .thenReturn(response);
        when(parameters.getAcceptedMediaTypes()).thenReturn(Collections.singletonList(MediaType.APPLICATION_JSON));

        response = rem.resource(TYPE, RESOURCE_ID, parameters,
                Optional.of(new ByteArrayInputStream(getEntityWithoutAcl().toString().getBytes())));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testPutRelation() throws IOException, AclFieldNotPresentException {
        InputStream entity = mock(InputStream.class);
        when(entity.available()).thenReturn(0);

        ResourceId resourceId = new ResourceId("idOrigin");

        when(getResponse.getStatus()).thenReturn(204);
        when(aclResourcesService.isAuthorized(eq(tokenInfo), eq(TYPE), eq(resourceId), eq(AclPermission.WRITE))).thenReturn(true);

        RelationParameters apiParameters = mock(RelationParameters.class);
        when(relationParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getPredicateResource()).thenReturn(Optional.of("idDist"));

        when(aclResourcesService.putRelation(any(), eq(TYPE), eq(resourceId), eq(TYPE), eq(relationParameters), any()))
                .thenReturn(getResponse);
        when(getResponse.getEntity()).thenReturn(entity);
        Response response = rem.relation(TYPE, resourceId, TYPE, relationParameters, Optional.of(entity));
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testPutRelationWithWildcardInOrigin() {
        ResourceId resourceId = new ResourceId("_");

        RelationParameters apiParameters = mock(RelationParameters.class);
        when(relationParameters.getOptionalApiParameters()).thenReturn(Optional.of(apiParameters));
        when(apiParameters.getPredicateResource()).thenReturn(Optional.of("idDst"));

        Response response = rem.relation(TYPE, resourceId, TYPE, relationParameters, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

}
