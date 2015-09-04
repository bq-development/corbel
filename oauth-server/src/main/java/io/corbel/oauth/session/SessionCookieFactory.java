package io.corbel.oauth.session;

import javax.ws.rs.core.NewCookie;

/**
 * @author Rubén Carrasco
 * 
 */
public interface SessionCookieFactory {

    String COOKIE_NAME = "SID";

    NewCookie createCookie(String sesion);

    NewCookie destroyCookie(String session);
}
