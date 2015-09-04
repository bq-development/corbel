package io.corbel.oauth.api;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.corbel.lib.token.reader.TokenReader;
import io.corbel.oauth.session.SessionCookieFactory;

/**
 * @author Alexander De Leon
 *
 */
@Path(ApiVersion.CURRENT + "/oauth/signout") public class SignoutResource {

    private final SessionCookieFactory sessionCookieFactory;

    public SignoutResource(SessionCookieFactory sessionCookieFactory) {
        this.sessionCookieFactory = sessionCookieFactory;
    }

    @GET
    public Response signout(@CookieParam(SessionCookieFactory.COOKIE_NAME) TokenReader session) {
        ResponseBuilder responseBuilder = Response.noContent();
        if (null != session) {
            String token = session.getToken();
            NewCookie cookie = sessionCookieFactory.destroyCookie(token);
            responseBuilder.cookie(cookie);
        }
        return responseBuilder.build();
    }

}
