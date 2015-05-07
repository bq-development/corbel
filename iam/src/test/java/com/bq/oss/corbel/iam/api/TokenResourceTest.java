package com.bq.oss.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.bq.oss.corbel.iam.exception.MissingBasicParamsException;
import com.bq.oss.corbel.iam.exception.MissingOAuthParamsException;
import com.bq.oss.corbel.iam.exception.OauthServerConnectionException;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.GrantType;
import com.bq.oss.corbel.iam.model.TokenGrant;
import com.bq.oss.corbel.iam.service.AuthorizationService;
import com.bq.oss.corbel.iam.service.UpgradeTokenService;
import com.bq.oss.corbel.iam.utils.TokenCookieFactory;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.reader.TokenReader;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.BearerTokenAuthenticator;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import io.dropwizard.auth.oauth.OAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Alexander De Leon
 * 
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
    private static final String TEST_ASSERTION = "123.456.789";
    private static final String TEST_TOKEN = "1|D|XXX";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TEST_USER_ID = "testUserId";

    private static final AuthorizationService authorizationServiceMock = mock(AuthorizationService.class);
    private static final UpgradeTokenService upgradeTokenServiceMock = mock(UpgradeTokenService.class);
    private static final TokenCookieFactory tokenCookieFactoryMock = mock(TokenCookieFactory.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final TokenInfo token = mock(TokenInfo.class);
    private static final TokenReader tokenReader = mock(TokenReader.class);

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new TokenResource(authorizationServiceMock, upgradeTokenServiceMock, tokenCookieFactoryMock))
            .addProvider(new OAuthProvider<>(authenticatorMock, null)).build();

    public TokenResourceTest() throws Exception {
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReader);
        when(tokenReader.getInfo()).thenReturn(token);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));
        when(token.getUserId()).thenReturn(TEST_USER_ID);
        when(token.toString()).thenReturn(TEST_TOKEN);
        NewCookie cookie = new NewCookie("token", TEST_TOKEN);
        when(tokenCookieFactoryMock.createCookie(Mockito.eq(TEST_TOKEN), Mockito.anyInt())).thenReturn(cookie);
    }

    @Test
    public void testPostReturnTokenOk() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenReturn(testTokenGrant);

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, formData);

        checkResponseContainsToken(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPostNotAuthorized() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenThrow(new UnauthorizedException(UNAUTHORIZED_MESSAGE));

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, formData);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void testPostMissingGrantType() throws SignatureException, UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(ASSERTION, TEST_ASSERTION);
        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).post(ClientResponse.class, formData);
        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testPostInvalidGrantType() throws SignatureException, UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, INVALID_GRANT_TYPE);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).post(ClientResponse.class, formData);
        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testPostMissingAssertion() throws SignatureException, UnauthorizedException {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);
        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).post(ClientResponse.class, formData);
        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testGetIsAuthenticated() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, GrantType.JWT_BEARER)
                .queryParam(ASSERTION, TEST_ASSERTION).queryParam(ACCESS_TOKEN, TEST_TOKEN).get(ClientResponse.class);

        checkResponseContainsToken(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @Test
    public void testGetInvalidGrantType() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, INVALID_GRANT_TYPE)
                .queryParam(ASSERTION, TEST_ASSERTION).queryParam(ACCESS_TOKEN, TEST_TOKEN).get(ClientResponse.class);

        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testGetMissingGrantType() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).queryParam(ASSERTION, TEST_ASSERTION)
                .queryParam(ACCESS_TOKEN, TEST_TOKEN).get(ClientResponse.class);

        checkErrorResponse(response, 400, INVALID_GRANT_ERROR_MESSAGE);
    }

    @Test
    public void testGetMissingAssertion() throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, GrantType.JWT_BEARER)
                .queryParam(ACCESS_TOKEN, TEST_TOKEN).get(ClientResponse.class);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testGetState() throws UnauthorizedException, MissingOAuthParamsException, UniformInterfaceException,
            ClientHandlerException, UnsupportedEncodingException, OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        ClientResponse response = RULE
                .client()
                .resource(OAUTH_TOKEN_ENDPOINT)
                .queryParam(GRANT_TYPE, "other")
                .queryParam(ASSERTION, "other")
                .queryParam(
                        "state",
                        ASSERTION + "=" + TEST_ASSERTION + "&" + GRANT_TYPE + "=" + URLEncoder.encode(GrantType.JWT_BEARER, "UTF-8")
                                + "&otherparam=othercontet").get(ClientResponse.class);

        checkResponseContainsToken(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @Test
    public void testUpgradeToken() throws UnauthorizedException {
        ClientResponse response = RULE.client().resource(UPGRADE_TOKEN_ENDPOINT).queryParam(ASSERTION, TEST_ASSERTION)
                .queryParam(GRANT_TYPE, GrantType.JWT_BEARER).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(204);
        verify(upgradeTokenServiceMock).upgradeToken(TEST_ASSERTION, tokenReader);
    }

    @Test
    public void testInvalidGrantTypeUpgradeToken() throws UnauthorizedException {
        ClientResponse response = RULE.client().resource(UPGRADE_TOKEN_ENDPOINT).queryParam(ASSERTION, TEST_ASSERTION)
                .queryParam(GRANT_TYPE, "").header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetMissingAssertionUpgradeToken() throws UnauthorizedException, MissingOAuthParamsException {
        ClientResponse response = RULE.client().resource(UPGRADE_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, "")
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testUpgradeTokenMissingAssertion() {
        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT + "/upgrade?" + ASSERTION + "=")
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);

        response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT + "/upgrade").header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(ClientResponse.class);

        checkErrorResponse(response, 400, INVALID_ASSERTION_ERROR_MESSAGE);
    }

    @Test
    public void testPostWithCookieReturnTokenOk() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(TEST_ASSERTION)).thenReturn(testTokenGrant);

        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(ASSERTION, TEST_ASSERTION);
        formData.add(GRANT_TYPE, GrantType.JWT_BEARER);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).header(REQUEST_COOKIE, true)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

        checkResponseContainsTokenWithCookie(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    @Test
    public void testGetWithCookieIsAuthenticated() throws UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException {
        TokenGrant testTokenGrant = new TokenGrant(TEST_TOKEN, 1, REFRESH_TOKEN);
        when(authorizationServiceMock.authorize(Mockito.eq(TEST_ASSERTION), Mockito.any())).thenReturn(testTokenGrant);

        ClientResponse response = RULE.client().resource(OAUTH_TOKEN_ENDPOINT).queryParam(GRANT_TYPE, GrantType.JWT_BEARER)
                .queryParam(ASSERTION, TEST_ASSERTION).queryParam(ACCESS_TOKEN, TEST_TOKEN).header(REQUEST_COOKIE, true)
                .get(ClientResponse.class);

        checkResponseContainsTokenWithCookie(response, TEST_TOKEN, REFRESH_TOKEN);
    }

    private void checkResponseContainsTokenWithCookie(ClientResponse response, String token, String refreshToken) {
        checkResponseContainsToken(response, token, refreshToken);
        assertThat(response.getCookies()).isNotEmpty();
        NewCookie cookie = response.getCookies().get(0);
        assertThat(cookie.getValue()).isEqualTo(token);
    }

    private void checkErrorResponse(ClientResponse response, int status, String expectedMessage) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        com.bq.oss.lib.ws.model.Error error = response.getEntity(com.bq.oss.lib.ws.model.Error.class);
        assertThat(error.getError()).isEqualTo(expectedMessage);
    }

    private void checkResponseContainsToken(ClientResponse response, String expectedToken, String expectedRefreshToken) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        TokenGrant grant = response.getEntity(TokenGrant.class);
        assertThat(grant.getAccessToken()).isEqualTo(expectedToken);
        assertThat(grant.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(grant.getExpiresAt()).isEqualTo(1);
    }

}
