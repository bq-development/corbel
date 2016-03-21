package io.corbel.iam.api;

import io.corbel.iam.model.User;
import io.corbel.iam.service.UserService;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.auth.AuthorizationInfoProvider;
import io.corbel.lib.ws.auth.AuthorizationRequestFilter;
import io.corbel.lib.ws.auth.BearerTokenAuthenticator;
import com.google.common.base.Optional;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.Instant;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Francisco Sanchez
 */
public class UsernameResourceTest extends UserResourceTestBase {

    private static final UserService userServiceMock = mock(UserService.class);
    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final TokenReader tokenReaderMock = mock(TokenReader.class);
    private static final TokenInfo tokenMock = mock(TokenInfo.class);

    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, "", false, "username"));

    private static final String URL_PREFIX = "/" + ApiVersion.CURRENT + "/" + TEST_DOMAIN_ID + "/username/";

    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new UsernameResource(userServiceMock))
            .addProvider(filter).addProvider(new AuthorizationInfoProvider().getBinder()).build();

    public UsernameResourceTest() throws Exception {
        when(tokenMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReaderMock);
        when(tokenReaderMock.getInfo()).thenReturn(tokenMock);
        when(tokenMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
    }

    @Test
    public void testExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(TEST_USERNAME, TEST_DOMAIN_ID)).thenReturn(true);
        Response response = RULE.client().target(URL_PREFIX + TEST_USERNAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .head();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNotExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(TEST_USERNAME, TEST_DOMAIN_ID)).thenReturn(false);
        Response response = RULE.client().target(URL_PREFIX + TEST_USERNAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .head();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetUserIdByUsernameOK() {
        User user = createTestUser();
        when(userServiceMock.findByDomainAndUsername(TEST_DOMAIN_ID, TEST_USERNAME)).thenReturn(user);
        User response = RULE.client().target(URL_PREFIX + TEST_USERNAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(User.class);
        verify(userServiceMock, times(1)).findByDomainAndUsername(TEST_DOMAIN_ID, TEST_USERNAME);

        assertEquals(response.getId(), TEST_USER_ID);
    }

    @Test
    public void testGetUserIdByUsernameKO() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findByDomainAndUsername(TEST_DOMAIN_ID, TEST_USERNAME)).thenReturn(user);
        Response response = RULE.client().target(URL_PREFIX + TEST_USERNAME + Instant.now().toString()).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
    }
}