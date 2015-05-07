package com.bq.oss.corbel.iam.api;

import java.net.URLDecoder;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import com.bq.oss.corbel.iam.auth.OauthParams;
import com.bq.oss.corbel.iam.exception.*;
import com.bq.oss.corbel.iam.model.GrantType;
import com.bq.oss.corbel.iam.model.TokenGrant;
import com.bq.oss.corbel.iam.service.AuthorizationService;
import com.bq.oss.corbel.iam.service.UpgradeTokenService;
import com.bq.oss.corbel.iam.utils.TokenCookieFactory;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.model.Error;
import com.google.common.base.Optional;
import io.dropwizard.auth.Auth;

/**
 * @author Alexander De Leon
 * 
 */
@Path(ApiVersion.CURRENT + "/oauth/token") public class TokenResource {

    private final AuthorizationService authorizationService;
    private final UpgradeTokenService upgradeTokenService;
    private final TokenCookieFactory tokenCookieFactory;

    public TokenResource(AuthorizationService authorizationService, UpgradeTokenService upgradeTokenService,
            TokenCookieFactory tokenCookieFactory) {
        this.authorizationService = authorizationService;
        this.upgradeTokenService = upgradeTokenService;
        this.tokenCookieFactory = tokenCookieFactory;
    }

    @POST
    public Response getToken(@FormParam("grant_type") String grantType, @FormParam("assertion") String assertion,
            @HeaderParam("RequestCookie") boolean cookie) {
        if (grantType == null || grantType.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingGrantType();
        }
        if (assertion == null || assertion.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingAssertion();
        }
        if (grantType.equals(GrantType.JWT_BEARER)) {
            return doJwtAuthorization(assertion, Optional.<OauthParams>absent(), cookie);
        }
        return IamErrorResponseFactory.getInstance().notSupportedGrantType(grantType);
    }

    @Path("/upgrade")
    @GET
    public Response upgradeToken(@Auth AuthorizationInfo authorizationInfo, @QueryParam("grant_type") String grantType,
            @QueryParam("assertion") String assertion) {

        if (assertion == null || assertion.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingAssertion();
        }
        if (grantType == null || grantType.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingGrantType();
        }
        if (grantType.equals(GrantType.JWT_BEARER)) {
            try {
                upgradeTokenService.upgradeToken(assertion, authorizationInfo.getTokenReader());
                return Response.noContent().build();
            } catch (UnauthorizedException e) {
                return IamErrorResponseFactory.getInstance().unauthorized(e.getMessage());
            }
        }
        return IamErrorResponseFactory.getInstance().notSupportedGrantType(grantType);
    }

    @GET
    public Response getTokenWithCode(@Context UriInfo uriInfo, @QueryParam("grant_type") String grantType,
            @QueryParam("assertion") String assertion, @QueryParam("access_token") String accessToken, @QueryParam("code") String code,
            @QueryParam("oauth_token") String token, @QueryParam("oauth_verifier") String verifier,
            @QueryParam("redirect_uri") String redirectUri, @QueryParam("state") String state, @HeaderParam("RequestCookie") boolean cookie) {

        if (state != null) {
            try {
                state = URLDecoder.decode(state, "UTF-8");
                String[] stateArray = state.split("&");
                for (String param : stateArray) {
                    String[] keyValueParam = param.split("=");
                    switch (keyValueParam[0]) {
                        case "assertion":
                            assertion = keyValueParam[1];
                            break;
                        case "grant_type":
                            grantType = URLDecoder.decode(keyValueParam[1], "UTF-8");
                    }
                }
            } catch (Exception e) {
                return ErrorResponseFactory.getInstance().badRequest();
            }
        }

        if (grantType == null || grantType.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingGrantType();
        }
        if (assertion == null || assertion.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingAssertion();
        }

        if (grantType.equals(GrantType.JWT_BEARER)) {
            OauthParams params = new OauthParams().setAccessToken(accessToken).setCode(code).setToken(token).setVerifier(verifier)
                    .setRedirectUri(redirectUri != null ? redirectUri : uriInfo.getAbsolutePath().toString());
            return doJwtAuthorization(assertion, Optional.of(params), cookie);
        }
        return IamErrorResponseFactory.getInstance().notSupportedGrantType(grantType);
    }

    private Response doJwtAuthorization(String assertion, Optional<OauthParams> params, boolean setCookie) {
        try {
            TokenGrant token = params.isPresent() ? authorizationService.authorize(assertion, params.get()) : authorizationService
                    .authorize(assertion);

            ResponseBuilder responseBuilder = Response.ok(token).type(MediaType.APPLICATION_JSON_TYPE);

            if (setCookie) {
                int maxAge = (int) ((token.getExpiresAt() - System.currentTimeMillis()) / 1000);
                responseBuilder.cookie(tokenCookieFactory.createCookie(token.getAccessToken(), maxAge));
            }

            return responseBuilder.build();
        } catch (NoSuchPrincipalException e) {
            return IamErrorResponseFactory.getInstance().noSuchPrincipal(e.getMessage());
        } catch (InvalidVersionException e) {
            return IamErrorResponseFactory.getInstance().unsupportedVersion(e.getMessage());
        } catch (UnauthorizedException e) {
            return IamErrorResponseFactory.getInstance().unauthorized(e.getMessage());
        } catch (MissingOAuthParamsException e) {
            return IamErrorResponseFactory.getInstance().missingOauthParms();
        } catch (OauthServerConnectionException e) {
            return IamErrorResponseFactory.getInstance().badGateway(
                    new Error("unavailable", "External OAuth Server fail: " + e.getOAuthService() + " " + e.getMessage()));
        } catch (MissingBasicParamsException e) {
            return IamErrorResponseFactory.getInstance().missingBasicParms();
        }

    }

}
