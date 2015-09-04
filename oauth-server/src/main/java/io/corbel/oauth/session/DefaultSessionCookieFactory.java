package io.corbel.oauth.session;

import javax.ws.rs.core.NewCookie;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultSessionCookieFactory implements SessionCookieFactory {

    private static final String DELETED = "deleted";
    private static final int COOKIE_VERSION = 1;

    private final String path;
    private final String domain;
    private final String comment;
    private final int maxAge;
    private final boolean secure;

    public DefaultSessionCookieFactory(String path, String domain, String comment, Integer maxAge, Boolean secure) {
        this.path = path;
        this.domain = domain;
        this.comment = comment;
        this.maxAge = maxAge != null ? maxAge : 0;
        this.secure = secure != null ? secure : false;
    }

    @Override
    public NewCookie createCookie(String session) {
        return new NewCookie(COOKIE_NAME, session, path, domain, COOKIE_VERSION, comment, maxAge, secure);
    }

    @Override
    public NewCookie destroyCookie(String session) {
        return new NewCookie(COOKIE_NAME, DELETED, path, domain, COOKIE_VERSION, comment, -1, secure);
    }
}
