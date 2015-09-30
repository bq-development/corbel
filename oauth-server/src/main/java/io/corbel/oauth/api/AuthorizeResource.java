package io.corbel.oauth.api;

import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.api.error.ErrorMessage;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.oauth.filter.FilterRegistry;
import io.corbel.oauth.filter.exception.AuthFilterException;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.ResponseType;
import io.corbel.oauth.model.User;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.UserService;
import io.corbel.oauth.session.SessionBuilder;
import io.corbel.oauth.session.SessionCookieFactory;
import io.corbel.oauth.token.TokenExpireTime;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Francisco Sanchez
 */
@Path(ApiVersion.CURRENT + "/oauth/authorize") public class AuthorizeResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizeResource.class);
    private final UserService userService;
    private final TokenFactory tokenFactory;
    private final ClientService clientService;
    private final SessionCookieFactory sessionCookieFactory;
    private final TokenExpireTime tokenExpireTime;
    private final SessionBuilder sessionBuilder;
    private final FilterRegistry filterRegistry;

    public AuthorizeResource(UserService userService, TokenFactory tokenFactory, ClientService clientService,
            SessionCookieFactory sessionCookieFactory, TokenExpireTime tokenExpireTime, SessionBuilder sessionBuilder,
            FilterRegistry filterRegistry) {
        this.userService = userService;
        this.tokenFactory = tokenFactory;
        this.clientService = clientService;
        this.sessionCookieFactory = sessionCookieFactory;
        this.tokenExpireTime = tokenExpireTime;
        this.sessionBuilder = sessionBuilder;
        this.filterRegistry = filterRegistry;
    }

    @GET
    public Response authorize(@QueryParam("response_type") String responseType, @QueryParam("client_id") String clientId,
            @QueryParam("redirect_uri") String redirectUri, @CookieParam(SessionCookieFactory.COOKIE_NAME) TokenReader session,
            @QueryParam("state") String state) {
        ResponseType tokenType = ResponseType.fromString(responseType);

        assertRequiredParameter(clientId, "client_id");

        Optional<Response> response = clientService.findByName(clientId).map(client -> {
            checkArguments(tokenType, client, redirectUri);
            return getTokenResponseFromSession(Optional.ofNullable(session), tokenType, client, redirectUri, Optional.ofNullable(state));
        }).orElse(Optional.of(ErrorResponseFactory.getInstance().unauthorized()));

        return response.orElse(ErrorResponseFactory.getInstance().notfound(new Error("not_found", "OAuth session not found")));
    }

    @POST
    public Response login(@FormParam("username") String username, @FormParam("password") String password,
            @FormParam("response_type") String responseType, @FormParam("client_id") String clientId,
            @FormParam("redirect_uri") String redirectUri, @FormParam("state") String state,
            @CookieParam(SessionCookieFactory.COOKIE_NAME) TokenReader session, MultivaluedMap<String, String> form) {

        ResponseType tokenType = ResponseType.fromString(responseType);
        Optional<String> stateOptional = Optional.ofNullable(state);
        assertRequiredParameter(clientId, "client_id");

        return clientService.findByName(clientId).map(client -> {
            try {
                filterRegistry.filter(username, password, clientId, client.getDomain(), form);
            } catch (AuthFilterException e) {
                return ErrorResponseFactory.getInstance().unauthorized(e.getMessage());
            }
            if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
                return tryLoginWithCookieSession(client, redirectUri, Optional.ofNullable(session), tokenType, stateOptional);
            } else {
                return tryLoginWithUserCredentials(username, password, client, redirectUri, tokenType, stateOptional);
            }
        }

        ).orElse(ErrorResponseFactory.getInstance().unauthorized());
    }

    private Response tryLoginWithCookieSession(Client client, String redirectUri, Optional<TokenReader> session, ResponseType tokenType,
            Optional<String> stateOptional) {
        return getTokenResponseFromSession(session, tokenType, client, redirectUri, stateOptional).orElse(
                ErrorResponseFactory.getInstance().unauthorized());
    }

    private Response tryLoginWithUserCredentials(String user, String password, Client client, String redirectUri, ResponseType tokenType,
            Optional<String> stateOptional) {
        checkPostArguments(user, password, tokenType, client, redirectUri);
        Optional<String> loggedUser = signinWithUsername(user, password, client.getDomain());
        if (loggedUser.isPresent()) {
            return doResponse(loggedUser.get(), tokenType, client, redirectUri, stateOptional);
        }
        loggedUser = signinWithEmail(user, password, client.getDomain());
        if (loggedUser.isPresent()) {
            return doResponse(loggedUser.get(), tokenType, client, redirectUri, stateOptional);
        }
        return ErrorResponseFactory.getInstance().unauthorized();
    }

    private Optional<String> signinWithUsername(String username, String password, String domain) {
        User user = userService.findByUserNameAndDomain(username, domain);
        return signinInternal(user, password);
    }

    private Optional<String> signinWithEmail(String email, String password, String domain) {
        User user = userService.getUserByEmailAndDomain(email, domain);
        return signinInternal(user, password);
    }

    private Optional<String> signinInternal(User user, String password) {
        if (user != null && user.checkPassword(password)) {
            return Optional.of(user.getId());
        }
        return Optional.empty();
    }

    private Optional<Response> getTokenResponseFromSession(Optional<TokenReader> session, ResponseType tokenType, Client client,
            String redirectUri, Optional<String> state) {
        if (session.isPresent()) {
            LOG.debug("Authorizing logged-in user");
            TokenInfo tokenInfo = session.get().getInfo();
            return Optional.of(doResponse(tokenInfo.getUserId(), tokenType, client, redirectUri, state));
        }
        return Optional.empty();
    }

    private Response doResponse(String userId, ResponseType responseType, Client client, String redirectUri, Optional<String> state) {
        String session = sessionBuilder.createNewSession(client.getName(), userId);
        try {
            TokenGrant token = createToken(userId, responseType, client.getName());
            NewCookie cookie = sessionCookieFactory.createCookie(session);
            if (responseType == ResponseType.TOKEN) {
                return Response.ok().entity(token).cookie(cookie).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                URI finalRedirectUri = buildRedirectUri(redirectUri, state, token.getAccessToken());
                return Response.seeOther(finalRedirectUri).cookie(cookie).build();
            }
        } catch (Exception e) {
            LOG.error("Unexpected error: {}", e.getMessage(), e);
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    private TokenGrant createToken(String userId, ResponseType responseType, String clientId) {
        TokenType tokenType = TokenType.valueOf(responseType.name());
        return tokenFactory.createToken(TokenInfo.newBuilder().setType(tokenType).setUserId(userId).setClientId(clientId).build(),
                tokenExpireTime.getTokenExpireTimeFromResponseType(tokenType));
    }

    private void checkPostArguments(String username, String password, ResponseType responseType, Client client, String redirectUri) {
        assertRequiredParameter(username, "username");
        assertRequiredParameter(password, "password");
        checkArguments(responseType, client, redirectUri);
    }

    private void checkArguments(ResponseType responseType, Client client, String redirectUri) {
        assertRequiredParameter(responseType, "response_type");
        assertValidResponseType(responseType);
        assertRequiredParameter(client, "client_id");
        if (responseType == ResponseType.CODE) {
            assertRequiredParameter(redirectUri, "redirect_uri");
            checkRedirectUri(client, redirectUri);
        }
    }

    private void checkRedirectUri(Client client, String redirectUri) {
        if (!clientService.verifyRedirectUri(redirectUri, client)) {
            throw new WebApplicationException(ErrorResponseFactory.getInstance().unauthorized("Invalid redirect URI"));
        }
    }

    private URI buildRedirectUri(String redirectUri, Optional<String> state, String token) {
        UriBuilder uriBuilder = new JerseyUriBuilder();
        uriBuilder.uri(URI.create(redirectUri));
        if (state.isPresent()) {
            uriBuilder.queryParam("state", state.get());
        }
        uriBuilder.queryParam("code", token);
        return uriBuilder.build();
    }

    private void assertRequiredParameter(Object value, String parameterName) {
        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value))) {
            throw new WebApplicationException(ErrorResponseFactory.getInstance().missingParameter(parameterName));
        }
    }

    private void assertValidResponseType(ResponseType responseType) {
        if (responseType == ResponseType.INVALID) {
            throw new WebApplicationException(ErrorResponseFactory.getInstance().badRequest(
                    new Error("invalid_response_type", ErrorMessage.BAD_REQUEST.getMessage())));
        }
    }

}
