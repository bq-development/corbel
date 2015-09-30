/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.oauth.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.provider.SessionProvider;
import io.corbel.oauth.filter.FilterRegistry;
import io.corbel.oauth.filter.exception.AuthFilterException;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.User;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.UserService;
import io.corbel.oauth.session.SessionBuilder;
import io.corbel.oauth.session.SessionCookieFactory;
import io.corbel.oauth.token.TokenExpireTime;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Alexander De Leon
 * 
 */
public class AuthorizeResourceTest {

    private static final String TEST_USERNAME = "userIdTest";
    private static final String TEST_PASSWORD = "userIdPassword";
    private static final String TEST_CLIENT_ID = "client";
    private static final String TEST_REDIRECT_URI = "http://example.org?a=b#frag";
    private static final String TEST_STATE = "123";
    private static final String TEST_TOKEN = "xxx";

    private static final TokenInfo TEST_TOKEN_INFO = TokenInfo.newBuilder().setType(TokenType.CODE).setUserId(TEST_USERNAME)
            .setClientId(TEST_CLIENT_ID).build();
    private static final TokenInfo TEST_TOKEN_INFO_TOKEN = TokenInfo.newBuilder().setType(TokenType.TOKEN).setUserId(TEST_USERNAME)
            .setClientId(TEST_CLIENT_ID).build();
    private static final long TEST_EXPIRES = 10;
    private static final String TEST_BAD_REDIRECT_URI = "asdf.com";

    private static final UserService userServiceMock = mock(UserService.class);
    private static final TokenFactory tokenFactory = mock(TokenFactory.class);
    private static final ClientService clientServiceMock = mock(ClientService.class);
    private static final SessionCookieFactory sessionCookieFactoryMock = mock(SessionCookieFactory.class);
    private static final SessionBuilder sessionBuilderMock = mock(SessionBuilder.class);
    private static final TokenParser tokenParserMock = mock(TokenParser.class);
    private static final FilterRegistry filterRegistryMock = mock(FilterRegistry.class);
    private static final TokenExpireTime tokenExpireTimeMock = mock(TokenExpireTime.class);
    private static final String TEST_DOMAIN = "TEST_DOMAIN";
    private static Client TEST_CLIENT = new Client();

    MultivaluedMap<String, String> formData;

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addResource(
                    new AuthorizeResource(userServiceMock, tokenFactory, clientServiceMock, sessionCookieFactoryMock, tokenExpireTimeMock,
                            sessionBuilderMock, filterRegistryMock)).addProvider(new SessionProvider(tokenParserMock).getBinder()).build();

    @Before
    public void before() {
        TEST_CLIENT = new Client();
        TEST_CLIENT.setDomain(TEST_DOMAIN);
        TEST_CLIENT.setName(TEST_CLIENT_ID);
        when(clientServiceMock.findByName(TEST_CLIENT_ID)).thenReturn(Optional.of(TEST_CLIENT));
        reset(filterRegistryMock);
    }

    public AuthorizeResourceTest() throws Exception {
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)).thenReturn(TEST_EXPIRES);
        formData = new MultivaluedHashMap();
        formData.add("username", TEST_USERNAME);
        formData.add("password", TEST_PASSWORD);
        formData.add("client_id", TEST_CLIENT_ID);
        formData.add("redirect_uri", TEST_REDIRECT_URI);
        formData.add("state", TEST_STATE);
        formData.add("response_type", "code");
    }

    @Test
    public void testLogin() {
        // CONFIGURE
        User userMock = mock(User.class);
        when(userServiceMock.findByUserNameAndDomain(TEST_USERNAME, TEST_DOMAIN)).thenReturn(userMock);
        when(userMock.checkPassword(TEST_PASSWORD)).thenReturn(true);
        when(userMock.getId()).thenReturn(TEST_USERNAME);
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)).thenReturn(TEST_EXPIRES);
        when(tokenFactory.createToken(TEST_TOKEN_INFO, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE))).thenReturn(
                new TokenGrant(TEST_TOKEN, TEST_EXPIRES));

        when(clientServiceMock.findByName(TEST_CLIENT_ID)).thenReturn(Optional.of(TEST_CLIENT));
        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(true);
        // LAUNCH
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize")
                .property(ClientProperties.FOLLOW_REDIRECTS, false).request().post(Entity.form(formData), Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(303);
        URI uri = response.getLocation();
        assertThat(uri.getHost()).isEqualTo("example.org");
        assertThat(uri.getQuery()).contains("a=b").contains("code=" + TEST_TOKEN);
        assertThat(uri.getFragment()).isEqualTo("frag");
    }

    @Test
    public void testLoginWithFailedFilters() throws AuthFilterException {
        doThrow(AuthFilterException.class).when(filterRegistryMock).filter(any(), any(), any(), any(), any());
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize")
                .property(ClientProperties.FOLLOW_REDIRECTS, false).request().post(Entity.form(formData), Response.class);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testLoginWithEmail() {
        User userMock = mock(User.class);
        when(userServiceMock.getUserByEmailAndDomain(TEST_USERNAME, TEST_DOMAIN)).thenReturn(userMock);
        when(userMock.checkPassword(TEST_PASSWORD)).thenReturn(true);
        when(userMock.getId()).thenReturn(TEST_USERNAME);
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)).thenReturn(TEST_EXPIRES);
        when(tokenFactory.createToken(TEST_TOKEN_INFO, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE))).thenReturn(
                new TokenGrant(TEST_TOKEN, TEST_EXPIRES));
        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(true);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize")
                .property(ClientProperties.FOLLOW_REDIRECTS, false).request().post(Entity.form(formData), Response.class);
        assertThat(response.getStatus()).isEqualTo(303);
        URI uri = response.getLocation();
        assertThat(uri.getHost()).isEqualTo("example.org");
        assertThat(uri.getQuery()).contains("a=b").contains("code=" + TEST_TOKEN);
        assertThat(uri.getFragment()).isEqualTo("frag");
    }

    @Test
    public void testLoginWithResponseTypeToken() {
        User userMock = mock(User.class);
        when(userServiceMock.findByUserNameAndDomain(TEST_USERNAME, TEST_DOMAIN)).thenReturn(userMock);
        when(userMock.checkPassword(TEST_PASSWORD)).thenReturn(true);
        when(userMock.getId()).thenReturn(TEST_USERNAME);
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.TOKEN)).thenReturn(TEST_EXPIRES);
        TokenGrant tokenGrant = new TokenGrant(TEST_TOKEN, TEST_EXPIRES);
        when(tokenFactory.createToken(TEST_TOKEN_INFO_TOKEN, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.TOKEN)))
                .thenReturn(tokenGrant);
        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(true);

        MultivaluedMap formData = new MultivaluedHashMap();
        formData.add("username", TEST_USERNAME);
        formData.add("password", TEST_PASSWORD);
        formData.add("client_id", TEST_CLIENT_ID);
        formData.add("state", TEST_STATE);
        formData.add("response_type", "token");

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").request()
                .post(Entity.form(formData), Response.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(TokenGrant.class)).isEqualTo(tokenGrant);
    }

    @Test
    public void testMissingResponseType() {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").queryParam("client_id", TEST_CLIENT_ID)
                .queryParam("redirect_uri", TEST_REDIRECT_URI).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testMissingClientId() {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").queryParam("response_type", "code")
                .queryParam("redirect_uri", TEST_REDIRECT_URI).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testMissingRedirectUri() {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").queryParam("response_type", "code")
                .queryParam("cient_id", TEST_CLIENT_ID).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testInvalidRedirectUri() {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").queryParam("response_type", "code")
                .queryParam("client_id", TEST_CLIENT_ID).queryParam("redirect_uri", TEST_BAD_REDIRECT_URI).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testInvalidResponseType() {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").queryParam("response_type", "fail")
                .queryParam("client_id", TEST_CLIENT_ID).queryParam("redirect_uri", TEST_REDIRECT_URI).request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
