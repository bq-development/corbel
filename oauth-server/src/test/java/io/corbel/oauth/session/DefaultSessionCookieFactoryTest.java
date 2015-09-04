/*
 * Copyright (C) 2014 StarTIC
 */
package io.corbel.oauth.session;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.ws.rs.core.NewCookie;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultSessionCookieFactoryTest {

    private static final String TEST_NAME = "SID";
    private static final int TEST_VERSION = 1;
    private static final String TEST_PATH = "/aPath";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_COMMENT = "comment";
    private static final Integer TEST_MAX_AGE = 3600;
    private static final Boolean TEST_SECURE = true;
    private static final String TEST_TAG = "sessiontag";

    private DefaultSessionCookieFactory factory;

    @Before
    public void setup() {
        factory = new DefaultSessionCookieFactory(TEST_PATH, TEST_DOMAIN, TEST_COMMENT, TEST_MAX_AGE, TEST_SECURE);
    }

    @Test
    public void testCookieCreation() {
        NewCookie cookie = factory.createCookie(TEST_TAG);
        assertCookieProperties(cookie);
        assertThat(cookie.getMaxAge()).isEqualTo(TEST_MAX_AGE);
        assertThat(cookie.getValue()).isEqualTo(TEST_TAG);
    }

    @Test
    public void testNullProperties() {
        factory = new DefaultSessionCookieFactory(null, null, null, null, null);
        NewCookie cookie = factory.createCookie(TEST_TAG);
        assertThat(cookie.getValue()).isEqualTo(TEST_TAG);
    }

    @Test
    public void testDestroy() {
        NewCookie cookie = factory.destroyCookie(TEST_TAG);
        assertCookieProperties(cookie);
        assertThat(cookie.getMaxAge()).isEqualTo(-1);
    }

    private void assertCookieProperties(NewCookie cookie) {
        assertThat(cookie.getComment()).isEqualTo(TEST_COMMENT);
        assertThat(cookie.getDomain()).isEqualTo(TEST_DOMAIN);
        assertThat(cookie.getName()).isEqualTo(TEST_NAME);
        assertThat(cookie.getPath()).isEqualTo(TEST_PATH);
        assertThat(cookie.getVersion()).isEqualTo(TEST_VERSION);
    }

}
