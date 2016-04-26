package io.corbel.iam.api;

import com.google.common.base.Optional;
import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.*;
import io.corbel.iam.model.GrantType;
import io.corbel.iam.model.TokenGrant;
import io.corbel.iam.model.TokenUpgradeGrant;
import io.corbel.iam.service.AuthorizationService;
import io.corbel.iam.service.UpgradeTokenService;
import io.corbel.iam.utils.TokenCookieFactory;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.model.Error;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander De Leon
 */
@Path(ApiVersion.CURRENT + "/oauth/token")
public class TokenResource {

    private final AuthorizationService authorizationService;
    private final UpgradeTokenService upgradeTokenService;
    private final TokenCookieFactory tokenCookieFactory;

    public TokenResource(AuthorizationService authorizationService, UpgradeTokenService upgradeTokenService,
                         TokenCookieFactory tokenCookieFactory) {
        this.authorizationService = authorizationService;
        this.upgradeTokenService = upgradeTokenService;
        this.tokenCookieFactory = tokenCookieFactory;
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
            OauthParams params = new OauthParams().setAccessToken(accessToken).setCode(code).setToken(token)
                    .setVerifier(verifier).setRedirectUri(redirectUri != null ? redirectUri
                            : uriInfo.getAbsolutePath().toString());
            return doJwtAuthorization(assertion, Optional.of(params), cookie);
        }
        return IamErrorResponseFactory.getInstance().notSupportedGrantType(grantType);
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
            return doJwtAuthorization(assertion, Optional.absent(), cookie);
        }
        return IamErrorResponseFactory.getInstance().notSupportedGrantType(grantType);
    }

    @Path("/upgrade")
    @GET
    public Response upgradeTokenGET(@Auth AuthorizationInfo authorizationInfo, @QueryParam("grant_type") String grantType,
                                    @QueryParam("assertion") String assertion) {

        return upgradeToken(authorizationInfo, grantType, assertion);
    }

    @Path("/upgrade")
    @POST
    public Response upgradeTokenPOST(@Auth AuthorizationInfo authorizationInfo, @FormParam("grant_type") String grantType,
                                     @FormParam("assertion") String assertion) {
        return upgradeToken(authorizationInfo, grantType, assertion);
    }

    private Response doJwtAuthorization(String assertion, Optional<OauthParams> params, boolean setCookie) {
        try {
            TokenGrant token = params.isPresent() ? authorizationService.authorize(assertion, params.get()) :
                    authorizationService.authorize(assertion);
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
        } catch (UnauthorizedTimeException e) {
            return IamErrorResponseFactory.getInstance().unauthorized("invalid_time", e.getMessage());
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

    private Response upgradeToken(@Auth AuthorizationInfo authorizationInfo, @QueryParam("grant_type") String grantType,
                                  @QueryParam("assertion") String assertion) {
        if (assertion == null || assertion.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingAssertion();
        }
        if (grantType == null || grantType.isEmpty()) {
            return IamErrorResponseFactory.getInstance().missingGrantType();
        }
        if (grantType.equals(GrantType.JWT_BEARER)) {
            try {
                Set<String> scopes = upgradeTokenService.getScopesFromTokenToUpgrade(assertion);
                upgradeTokenService.upgradeToken(assertion, authorizationInfo.getTokenReader(), scopes);
                return Response.ok(new TokenUpgradeGrant(scopes)).type(MediaType.APPLICATION_JSON_TYPE).build();
            } catch (UnauthorizedException e) {
                return IamErrorResponseFactory.getInstance().unauthorized(e.getMessage());
            }
        }
        return IamErrorResponseFactory.getInstance().notSupportedGrantType(grantType);
    }
}
