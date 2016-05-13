package io.corbel.resources.rem.acl;

import static io.corbel.resources.rem.acl.AclTestUtils.getEntityWithoutAcl;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;
import io.corbel.resources.rem.service.RemService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;

/**
 * @author Cristian del Cerro
 */

@RunWith(MockitoJUnitRunner.class) public class AclPostRemTest {

    private static final String USER_ID = "userId";
    private static final String TYPE = "type";
    private static final String ID = "resourceId";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    private static final MultivaluedMap<String, Object> POST_METADATA = new StringKeyIgnoreCaseMultivaluedMap<>();

    private AclPostRem rem;

    @Mock private AclResourcesService aclResourcesService;
    @Mock private RemService remService;
    @Mock private RequestParameters<CollectionParameters> parameters;
    @Mock private TokenInfo tokenInfo;
    @Mock private Response getResponse;
    @Mock private Response postResponse;
    @Mock private Response putResponse;

    @Before
    public void setUp() throws Exception {
        POST_METADATA.add("Location", ID);
        when(getResponse.getStatus()).thenReturn(200);
        when(postResponse.getMetadata()).thenReturn(POST_METADATA);
        when(aclResourcesService.getResource(any(), eq(TYPE), eq(RESOURCE_ID), any(), any())).thenReturn(getResponse);
        when(aclResourcesService.saveResource(any(), any(), eq(TYPE), any(), any(), any())).thenReturn(postResponse);
        when(aclResourcesService.updateResource(any(), eq(TYPE), any(), any(), any(), any())).thenReturn(putResponse);
        rem = new AclPostRem(aclResourcesService, Collections.emptyList());
        rem.setRemService(remService);

        when(tokenInfo.getUserId()).thenReturn(USER_ID);
        when(parameters.getTokenInfo()).thenReturn(tokenInfo);
    }

    @Test
    public void testNoUserId() throws URISyntaxException {
        when(tokenInfo.getUserId()).thenReturn(null);
        when(parameters.getTokenInfo()).thenReturn(tokenInfo);
        Response response = rem.collection(TYPE, parameters, new URI("test"), Optional.empty(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    public void testEmptyObject() throws URISyntaxException, IOException {
        InputStream entity = mock(InputStream.class);
        when(parameters.getAcceptedMediaTypes()).thenReturn(AclBaseRem.JSON_MEDIATYPE);
        Response response = rem.collection(TYPE, parameters, new URI("test"), Optional.of(entity), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testCorrectNotJsonEntity() throws URISyntaxException {
        URI uri = new URI("test");
        JsonObject entity = getEntityWithoutAcl();
        rem.collection(TYPE, parameters, new URI("test"), Optional.of(new ByteArrayInputStream(entity.toString().getBytes())), Optional.empty());

        JsonObject aclValue = new JsonObject();
        aclValue.addProperty(DefaultAclResourcesService.PERMISSION, "ADMIN");
        aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        JsonObject acl = new JsonObject();
        acl.add(DefaultAclResourcesService.USER_PREFIX + USER_ID, aclValue);

        JsonObject jsonObjectToSaveOnResmi = new JsonObject();
        jsonObjectToSaveOnResmi.add("_acl", acl);

        when(putResponse.getStatus()).thenReturn(204);
        verify(aclResourcesService).saveResource(null, parameters, TYPE, uri, jsonObjectToSaveOnResmi, Collections.singletonList(rem));

    }

    @Test
    public void testExcludedRems() throws URISyntaxException {
        URI uri = new URI("test");
        JsonObject entity = getEntityWithoutAcl();
        Rem remMock = mock(Rem.class);
        rem.collection(TYPE, parameters, new URI("test"), Optional.of(new ByteArrayInputStream(entity.toString().getBytes())), Optional.of(Collections.singletonList(remMock)));

        JsonObject aclValue = new JsonObject();
        aclValue.addProperty(DefaultAclResourcesService.PERMISSION, "ADMIN");
        aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        JsonObject acl = new JsonObject();
        acl.add(DefaultAclResourcesService.USER_PREFIX + USER_ID, aclValue);

        JsonObject jsonObjectToSaveOnResmi = new JsonObject();
        jsonObjectToSaveOnResmi.add("_acl", acl);

        when(putResponse.getStatus()).thenReturn(204);
        verify(aclResourcesService).saveResource(null, parameters, TYPE, uri, jsonObjectToSaveOnResmi, Arrays.asList(rem, remMock));

    }

    @Test
    public void testCorrectJsonEntity() throws URISyntaxException {
        URI uri = new URI("test");
        JsonObject entity = getEntityWithoutAcl();
        when(parameters.getAcceptedMediaTypes()).thenReturn(AclBaseRem.JSON_MEDIATYPE);
        rem.collection(TYPE, parameters, new URI("test"), Optional.of(new ByteArrayInputStream(entity.toString().getBytes())), Optional.empty());

        JsonObject aclValue = new JsonObject();
        aclValue.addProperty(DefaultAclResourcesService.PERMISSION, "ADMIN");
        aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());

        JsonObject acl = new JsonObject();
        acl.add(DefaultAclResourcesService.USER_PREFIX + USER_ID, aclValue);

        entity.add("_acl", acl);
        verify(aclResourcesService).saveResource(null, parameters, TYPE, uri, entity, Collections.singletonList(rem));
        verify(aclResourcesService, times(0)).updateResource(null, TYPE, new ResourceId(ID), null, entity, Collections.singletonList(rem));
    }

}
