package io.corbel.iam.utils;

import javax.ws.rs.core.NewCookie;

/**
 * @author Rubén Carrasco
 * 
 */
public interface TokenCookieFactory {
    public static String NAME = "token";

    NewCookie createCookie(String token, int maxAge);
}
