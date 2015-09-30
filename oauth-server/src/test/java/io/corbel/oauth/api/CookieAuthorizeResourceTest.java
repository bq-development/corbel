/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.oauth.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.provider.SessionProvider;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.oauth.filter.FilterRegistry;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.User;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.UserService;
import io.corbel.oauth.session.SessionBuilder;
import io.corbel.oauth.session.SessionCookieFactory;
import io.corbel.oauth.token.TokenExpireTime;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Alexander De Leon
 */
public class CookieAuthorizeResourceTest {

    private static final String TEST_COOKIE = "micookie";

    private static final String TEST_USERNAME = "usernameTest";
    private static final String TEST_PASSWORD = "userIdPassword";
    private static final String TEST_CLIENT_ID = "client";
    private static Client TEST_CLIENT = new Client();
    private static final String TEST_REDIRECT_URI = "http://example.org?a=b#frag";
    private static final String TEST_STATE = "123";
    private static final String TEST_TOKEN = "xxx";
    private static final TokenInfo TEST_TOKEN_INFO = TokenInfo.newBuilder().setType(TokenType.CODE).setUserId(TEST_USERNAME)
            .setClientId(TEST_CLIENT_ID).build();
    private static final long TEST_EXPIRES = 10;

    private static final int TEST_COOKIE_VERSION = 1;

    private static final UserService userServiceMock = mock(UserService.class);
    private static final TokenFactory tokenFactoryMock = mock(TokenFactory.class);
    private static final ClientService clientServiceMock = mock(ClientService.class);
    private static final TokenParser tokenParserMock = mock(TokenParser.class);
    private static final TokenReader tokenReaderMock = mock(TokenReader.class);
    private static final SessionBuilder sessionBuilderMock = mock(SessionBuilder.class);
    private static final SessionCookieFactory sessionCookieFactoryMock = mock(SessionCookieFactory.class);
    private static final TokenExpireTime tokenExpireTimeMock = mock(TokenExpireTime.class);
    private static final FilterRegistry filterRegistryMock = mock(FilterRegistry.class);
    private static final String TEST_DOMAIN = "TEST_DOMAIN";

    MultivaluedMap<String, String> formData;

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addResource(
                    new AuthorizeResource(userServiceMock, tokenFactoryMock, clientServiceMock, sessionCookieFactoryMock,
                            tokenExpireTimeMock, sessionBuilderMock, filterRegistryMock)).addProvider(GenericExceptionMapper.class)
            .addProvider(new SessionProvider(tokenParserMock).getBinder()).build();

    public CookieAuthorizeResourceTest() throws Exception {
        formData = new MultivaluedHashMap();
        formData.add("username", TEST_USERNAME);
        formData.add("password", TEST_PASSWORD);
        formData.add("client_id", TEST_CLIENT_ID);
        formData.add("redirect_uri", TEST_REDIRECT_URI);
        formData.add("state", TEST_STATE);
        formData.add("response_type", "code");
        when(tokenReaderMock.getInfo()).thenReturn(TokenInfo.newBuilder().setUserId(TEST_USERNAME).setClientId(TEST_CLIENT_ID).build());
    }

    @Before
    public void before() {
        TEST_CLIENT = new Client();
        TEST_CLIENT.setDomain(TEST_DOMAIN);
        TEST_CLIENT.setName(TEST_CLIENT_ID);
        when(clientServiceMock.findByName(TEST_CLIENT_ID)).thenReturn(Optional.of(TEST_CLIENT));
    }

    @Test
    public void testCookie() {
        User userMock = mock(User.class);
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)).thenReturn(TEST_EXPIRES);
        when(userServiceMock.findByUserNameAndDomain(TEST_USERNAME, TEST_DOMAIN)).thenReturn(userMock);
        when(userMock.checkPassword(TEST_PASSWORD)).thenReturn(true);
        when(userMock.getId()).thenReturn(TEST_USERNAME);
        when(tokenFactoryMock.createToken(TEST_TOKEN_INFO, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)))
                .thenReturn(new TokenGrant(TEST_TOKEN, TEST_EXPIRES));
        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(true);

        NewCookie cookie = new NewCookie(SessionCookieFactory.COOKIE_NAME, TEST_COOKIE, null, null, TEST_COOKIE_VERSION, null, 100, false);

        when(sessionBuilderMock.createNewSession(TEST_CLIENT_ID, TEST_USERNAME)).thenReturn(TEST_COOKIE);
        when(sessionCookieFactoryMock.createCookie(TEST_COOKIE)).thenReturn(cookie);

        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(true);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize")
                .property(ClientProperties.FOLLOW_REDIRECTS, false).request().post(Entity.form(formData), Response.class);
        assertThat(response.getStatus()).isEqualTo(303);

        assertThat(response.getCookies().get("SID")).isEqualTo(cookie);
    }

    @Test
    public void testGetWithCookie() throws TokenVerificationException {
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)).thenReturn(TEST_EXPIRES);
        when(tokenParserMock.parseAndVerify(TEST_COOKIE)).thenReturn(tokenReaderMock);
        when(userServiceMock.findByUserNameAndDomain(TEST_USERNAME, TEST_DOMAIN)).thenReturn(new User());
        when(tokenFactoryMock.createToken(TEST_TOKEN_INFO, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)))
                .thenReturn(new TokenGrant(TEST_TOKEN, TEST_EXPIRES));
        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(true);

        Cookie firstCookie = new Cookie(SessionCookieFactory.COOKIE_NAME, TEST_COOKIE);
        Response response = doGetWithCookie(firstCookie);

        assertThat(response.getStatus()).isEqualTo(303);
    }

    @Test
    public void testInvalidUriWithCookie() {
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.CODE)).thenReturn(TEST_EXPIRES);
        when(userServiceMock.findByUserNameAndDomain(TEST_USERNAME, TEST_DOMAIN)).thenReturn(new User());
        when(tokenFactoryMock.createToken(TEST_TOKEN_INFO, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.TOKEN)))
                .thenReturn(new TokenGrant(TEST_TOKEN, TEST_EXPIRES));
        when(clientServiceMock.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).thenReturn(false);

        Cookie firstCookie = new Cookie(SessionCookieFactory.COOKIE_NAME, TEST_COOKIE);
        Response response = doGetWithCookie(firstCookie);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    private Response doGetWithCookie(Cookie cookie) {
        return RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/authorize").property(ClientProperties.FOLLOW_REDIRECTS, false)
                .queryParam("response_type", "code").queryParam("client_id", TEST_CLIENT_ID).queryParam("redirect_uri", TEST_REDIRECT_URI)
                .request().cookie(cookie).get(Response.class);
    }
}
