/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.oauth.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.junit.ClassRule;
import org.junit.Test;

import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.provider.SessionProvider;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.oauth.session.SessionCookieFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Alexander De Leon
 *
 */
public class SignoutResourceTest {

    private static final String TEST_SESSION = "123";

    private static final SessionCookieFactory sessionCookieFactoryMock = mock(SessionCookieFactory.class);
    private static final TokenParser tokenParserMock = mock(TokenParser.class);
    private static final TokenReader tokenReaderMock = mock(TokenReader.class);

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new SignoutResource(sessionCookieFactoryMock))
            .addProvider(new SessionProvider(tokenParserMock).getBinder()).build();

    public SignoutResourceTest() throws Exception {
        when(tokenReaderMock.getToken()).thenReturn(TEST_SESSION);
        when(tokenParserMock.parseAndVerify(TEST_SESSION)).thenReturn(tokenReaderMock);
    }

    @Test
    public void testSingoutWithoutCookie() {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/signout").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(response.getCookies()).isEmpty();
    }

    @Test
    public void testSingoutWithCookie() {

        NewCookie destroyCookie = new NewCookie(SessionCookieFactory.COOKIE_NAME, TEST_SESSION);
        when(sessionCookieFactoryMock.destroyCookie(TEST_SESSION)).thenReturn(destroyCookie);

        Cookie cookie = new Cookie(SessionCookieFactory.COOKIE_NAME, TEST_SESSION);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/oauth/signout").request().cookie(cookie).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(response.getCookies().get("SID")).isEqualTo(destroyCookie);
    }
}
