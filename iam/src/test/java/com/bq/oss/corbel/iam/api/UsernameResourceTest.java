package com.bq.oss.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.ClassRule;
import org.junit.Test;

import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.reader.TokenReader;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.AuthorizationInfoProvider;
import com.bq.oss.lib.ws.auth.AuthorizationRequestFilter;
import com.bq.oss.lib.ws.auth.BearerTokenAuthenticator;
import com.google.common.base.Optional;

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

    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, ""));

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new UsernameResource(userServiceMock))
            .addProvider(filter).addProvider(new AuthorizationInfoProvider().getBinder()).build();

    public UsernameResourceTest() throws Exception {
        when(tokenMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReaderMock);
        when(tokenReaderMock.getInfo()).thenReturn(tokenMock);
        when(tokenMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkAccessRules(eq(authorizationInfoMock), any());
    }

    @Test
    public void testExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(TEST_USERID, TEST_DOMAIN_ID)).thenReturn(true);
        Response response = RULE.client().target("/v1.0/username/" + TEST_USERID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .head();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNotExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(TEST_USERID, TEST_DOMAIN_ID)).thenReturn(false);
        Response response = RULE.client().target("/v1.0/username/" + TEST_USERID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .head();

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
