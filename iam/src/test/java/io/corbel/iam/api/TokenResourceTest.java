package io.corbel.iam.api;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.corbel.iam.exception.*;
import io.corbel.iam.model.GrantType;
import io.corbel.iam.model.TokenGrant;
import io.corbel.iam.service.AuthorizationService;
import io.corbel.iam.service.UpgradeTokenService;
import io.corbel.iam.utils.TokenCookieFactory;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.*;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Alexander De Leon
 */
public class TokenResourceTest {

    private static final String REQUEST_COOKIE = "RequestCookie";
    private static final String OAUTH_TOKEN_ENDPOINT = "/v1.0/oauth/token";
    private static final String UPGRADE_TOKEN_ENDPOINT = "/v1.0/oauth/token/upgrade";
    private static final String ASSERTION = "assertion";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String INVALID_GRANT_TYPE = "blablabla";
    private static final String UNAUTHORIZED_MESSAGE = "message";
    private static final String INVALID_GRANT_ERROR_MESSAGE = "invalid_grant";
    private static final String INVALID_ASSERTION_ERROR_MESSAGE = "invalid_grant";
    private static final String INVALID_TIME_ERROR_MESSAGE = "invalid_time";
    private static final String TEST_ASSERTION = "123.456.789";
    private static final String TEST_TOKEN = "1|D|XXX";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TEST_USER_ID = "testUserId";
    private static final String SCOPE = "scope:test";
    private static final Set<String> SCOPES = Sets.newHashSet(SCOPE);

    private static final AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
    private static final UpgradeTokenService upgradeTokenServiceMock = mock(UpgradeTokenService.class);
    private static final TokenCookieFactory tokenCookieFactoryMock = mock(TokenCookieFactory.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final TokenInfo token = mock(TokenInfo.class);
    private static final TokenReader tokenReader = mock(TokenReader.class);

    private static final Authenticator<String, AuthorizationInfo> authenticator = mock(Authenticator.class);
    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticator, "realm", AuthorizationInfo.class);
    private static CookieOAuthFactory<AuthorizationInfo> cookieOAuthProvider = new CookieOAuthFactory<AuthorizationInfo>(authenticator,
            "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, cookieOAuthProvider,
            "v.*/oauth/token", false, ""));

    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new TokenResource(authorizationServiceMock, upgradeTokenServiceMock, tokenCookieFactoryMock))
            .addProvider(filter)
            .addProvider(new AuthorizationInfoProvider().getBinder()).build();
    public TokenResourceTest() throws Exception {
    }

    @Before
    public void setUp() throws AuthenticationException {
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReader);
        when(tokenReader.getInfo()).thenReturn(token);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));
        when(token.getUserId()).thenReturn(TEST_USER_ID);
        when(token.toString()).thenReturn(TEST_TOKEN);
        NewCookie cookie = new NewCookie("token", TEST_TOKEN);
        when(tokenCookieFactoryMock.createCookie(Mockito.eq(TEST_TOKEN), Mockito.anyInt())).thenReturn(cookie);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(authenticator.authenticate(any())).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
        reset(upgradeTokenServiceMock);
        reset(authorizationServiceMock);
    }

    @Test
    public void testPostReturnTokenOk() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenReturn(testTokenGrant);

        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.form(formData), Response.class);

        checkResponseContainsToken(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPostNotAuthorized() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenThrow(new UnauthorizedException(UNAUTHORIZED_MESSAGE));

        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().post(Entity.form(formData), Response.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPostBadSystemClock() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenThrow(new UnauthorizedTimeException("IllegalExpireTimeException"));

        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().post(Entity.form(formData), Response.class);

        checkErrorResponse(response, 401, INVALID_TIME_ERROR_MESSAGE);
    }

    @Test
    public void testPostMissingGrantType() throws SignatureException, UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(ASSERTION, TEST_ASSERTION);
        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().post(Entity.form(formData), Response.class);
        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testPostInvalidGrantType() throws SignatureException, UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, INVALID_GRANT_TYPE);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().post(Entity.form(formData), Response.class);
        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testPostMissingAssertion() throws SignatureException, UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);
        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.form(formData), Response.class);
        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testGetIsAuthenticated() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, GrantType.JWT_BEARER)
                .queryParam(ASSERTION, TEST_ASSERTION).queryParam(ACCESS_TOKEN, TEST_TOKEN).request().get(Response.class);

        checkResponseContainsToken(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @Test
    public void testGetInvalidGrantType() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, INVALID_GRANT_TYPE)
                .queryParam(ASSERTION, TEST_ASSERTION).queryParam(ACCESS_TOKEN, TEST_TOKEN).request().get(Response.class);

        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testGetMissingGrantType() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).queryParam(ASSERTION, TEST_ASSERTION)
                .queryParam(ACCESS_TOKEN, TEST_TOKEN).request().get(Response.class);

        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testGetMissingAssertion() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, GrantType.JWT_BEARER)
                .queryParam(ACCESS_TOKEN, TEST_TOKEN).request().get(Response.class);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testGetState() throws UnauthorizedException, MissingOAuthParamsException, UnsupportedEncodingException,
            OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        Response response = RULE
                .client()
                .target(OAUTH_TOKEN_ENDPOINT)
                .queryParam(GRANT_TYPE, "other")
                .queryParam(ASSERTION, "other")
                .queryParam(
                        "state",
                        ASSERTION + "=" + TEST_ASSERTION + "&" + GRANT_TYPE + "=" + URLEncoder.encode(GrantType.JWT_BEARER, "UTF-8")
                                + "&otherparam=othercontet").request().get(Response.class);

        checkResponseContainsToken(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @Test
    public void testUpgradeToken() throws UnauthorizedException {
        Response response = RULE.client().target(UPGRADE_TOKEN_ENDPOINT).queryParam(ASSERTION, TEST_ASSERTION)
                .queryParam(GRANT_TYPE, GrantType.JWT_BEARER).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);

        Set<String> scopes = upgradeTokenServiceMock.getScopesFromTokenToUpgrade(ASSERTION);
        verify(upgradeTokenServiceMock).upgradeToken(TEST_ASSERTION, tokenReader, scopes);
    }

    @Test
    public void testUpgradeTokenPost() throws UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);
        formData.add(ASSERTION, TEST_ASSERTION);
        Response response = RULE.client().target(UPGRADE_TOKEN_ENDPOINT).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.form(formData), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);

        Set<String> scopes = upgradeTokenServiceMock.getScopesFromTokenToUpgrade(ASSERTION);
        verify(upgradeTokenServiceMock).upgradeToken(TEST_ASSERTION, tokenReader, scopes);
    }

    @Test
    public void testInvalidGrantTypeUpgradeToken() throws UnauthorizedException {
        Response response = RULE.client().target(UPGRADE_TOKEN_ENDPOINT).queryParam(ASSERTION, TEST_ASSERTION).queryParam(GRANT_TYPE, "")
                .request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetMissingAssertionUpgradeToken() throws UnauthorizedException, MissingOAuthParamsException {
        Response response = RULE.client().target(UPGRADE_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, "").request()
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(400);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testUpgradeTokenMissingAssertion() {
        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT + "/upgrade?" + ASSERTION + "=").request()
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);

        response = RULE.client().target(OAUTH_TOKEN_ENDPOINT + "/upgrade").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(Response.class);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testPostWithCookieReturnTokenOk() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenReturn(testTokenGrant);

        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).request().header(REQUEST_COOKIE, true)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.form(formData), Response.class);

        checkResponseContainsTokenWithCookie(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @Test
    public void testGetWithCookieIsAuthenticated() throws UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN, SCOPES);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        Response response = RULE.client().target(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, GrantType.JWT_BEARER)
                .queryParam(ASSERTION, TEST_ASSERTION).queryParam(ACCESS_TOKEN, TEST_TOKEN).request().header(REQUEST_COOKIE, true)
                .get(Response.class);

        checkResponseContainsTokenWithCookie(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    private void checkResponseContainsTokenWithCookie(Response response, String token, String refreshToken) {
        checkResponseContainsToken(response, token, refreshToken);
        assertThat(response.getCookies()).isNotEmpty();
        NewCookie cookie = response.getCookies().get("token");
        assertThat(cookie.getValue()).isEqualTo(token);
    }

    private void checkErrorResponse(Response response, int status, String expectedMessage) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        io.corbel.lib.ws.model.Error error = response.readEntity(io.corbel.lib.ws.model.Error.class);
        assertThat(error.getError()).isEqualTo(expectedMessage);
    }

    private void checkResponseContainsToken(Response response, String expectedToken, String expectedRefreshToken) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        TokenGrant grant = response.readEntity(TokenGrant.class);
        assertThat(grant.getAccessToken()).isEqualTo(expectedToken);
        assertThat(grant.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(grant.getExpiresAt()).isEqualTo(1);
        assertThat(grant.getScopes()).containsExactly(SCOPE);
    }

}
