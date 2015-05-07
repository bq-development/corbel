package com.bq.oss.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.ClassRule;
import org.junit.Test;

import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.reader.TokenReader;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.BearerTokenAuthenticator;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.ClientResponse;

import io.dropwizard.auth.oauth.OAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Francisco Sanchez
 * 
 */
public class UsernameResourceTest extends UserResourceTestBase {

    private static final UserService userServiceMock = mock(UserService.class);
    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final TokenReader tokenReaderMock = mock(TokenReader.class);
    private static final TokenInfo tokenMock = mock(TokenInfo.class);

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new UsernameResource(userServiceMock))
            .addProvider(new OAuthProvider<>(authenticatorMock, null)).build();

    public UsernameResourceTest() throws Exception {
        when(tokenMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReaderMock);
        when(tokenReaderMock.getInfo()).thenReturn(tokenMock);
        when(tokenMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));
    }

    @Test
    public void testExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(TEST_USERID, TEST_DOMAIN_ID)).thenReturn(true);
        ClientResponse response = RULE.client().resource("/v1.0/username/" + TEST_USERID).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .head();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNotExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(TEST_USERID, TEST_DOMAIN_ID)).thenReturn(false);
        ClientResponse response = RULE.client().resource("/v1.0/username/" + TEST_USERID).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .head();

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
