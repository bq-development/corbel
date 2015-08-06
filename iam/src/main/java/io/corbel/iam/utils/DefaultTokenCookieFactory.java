package io.corbel.iam.utils;

import javax.ws.rs.core.NewCookie;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultTokenCookieFactory implements TokenCookieFactory {

    private static final int COOKIE_VERSION = 1;

    private final String path;
    private final String domain;
    private final String comment;
    private final boolean secure;

    public DefaultTokenCookieFactory(String path, String domain, String comment, Boolean secure) {
        this.path = path;
        this.domain = domain;
        this.comment = comment;
        this.secure = secure != null ? secure : false;
    }

    @Override
    public NewCookie createCookie(String token, int maxAge) {
        return new NewCookie(NAME, token, path, domain, COOKIE_VERSION, comment, maxAge, secure);
    }
}
