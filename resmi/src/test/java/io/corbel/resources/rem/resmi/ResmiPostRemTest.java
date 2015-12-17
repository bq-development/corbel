package io.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.resources.rem.model.ResourceUri;
import org.junit.Before;
import org.junit.Test;

import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import io.corbel.lib.token.TokenInfo;
import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public class ResmiPostRemTest extends ResmiRemTest {

    private AbstractResmiRem postRem;

    @Override
    @Before
    public void setup() {
        super.setup();
        postRem = new ResmiPostRem(resmiServiceMock);
    }

    @Test
    public void testPostCollectionWithUserToken() throws URISyntaxException, StartsWithUnderscoreException {
        ResourceUri uri = new ResourceUri(DOMAIN, TEST_TYPE);

        RequestParameters<CollectionParameters> requestParameters = mock(RequestParameters.class);
        TokenInfo tokenInfo = mock(TokenInfo.class);
        when(requestParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(requestParameters.getRequestedDomain()).thenReturn(DOMAIN);

        when(tokenInfo.getUserId()).thenReturn("userId");

        JsonObject testResource = getTestResource();
        when(resmiServiceMock.saveResource(eq(uri), eq(testResource), any())).thenReturn(testResource);

        Response response = postRem.collection(TEST_TYPE, requestParameters, new URI("test"), Optional.of(testResource));

        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testPostCollectionWithBadAttributeName() throws URISyntaxException, StartsWithUnderscoreException {
        RequestParameters<CollectionParameters> requestParameters = mock(RequestParameters.class);
        TokenInfo tokenInfo = mock(TokenInfo.class);
        when(requestParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(tokenInfo.getUserId()).thenReturn("userId");

        JsonObject testResource = getTestResource();

        doThrow(new StartsWithUnderscoreException("_any")).when(resmiServiceMock).saveResource(any(), eq(testResource), any());

        Response response = postRem.collection(TEST_TYPE, requestParameters, new URI("test"), Optional.of(testResource));

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testPostCollectionWithClientToken() throws URISyntaxException, StartsWithUnderscoreException {
        ResourceUri uri = new ResourceUri(DOMAIN, TEST_TYPE);

        RequestParameters<CollectionParameters> requestParameters = mock(RequestParameters.class);
        TokenInfo tokenInfo = mock(TokenInfo.class);
        when(requestParameters.getTokenInfo()).thenReturn(tokenInfo);
        when(requestParameters.getRequestedDomain()).thenReturn(DOMAIN);
        when(tokenInfo.getUserId()).thenReturn(null);

        JsonObject testResource = getTestResource();
        when(resmiServiceMock.saveResource(eq(uri), eq(testResource), any())).thenReturn(testResource);

        Response response = postRem.collection(TEST_TYPE, requestParameters, new URI("test"), Optional.of(testResource));

        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testInvalidPostCollection() {
        Response response = postRem.collection(TEST_TYPE, getParametersWithEmptyUri(), null, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testMethodNotAllowed() {
        Response response = postRem.resource(TEST_TYPE, TEST_ID, getParametersWithEmptyUri(), Optional.empty());
        assertThat(response.getStatus()).isEqualTo(405);
    }

}
