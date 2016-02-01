package io.corbel.iam.service;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.auth.AuthorizationRequestContextFactory;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.auth.BasicParams;
import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.auth.provider.AuthorizationProviderFactory;
import io.corbel.iam.auth.provider.Provider;
import io.corbel.iam.exception.MissingBasicParamsException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.NoSuchPrincipalException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.exception.UnauthorizedTimeException;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Identity;
import io.corbel.iam.model.Scope;
import io.corbel.iam.model.TokenGrant;
import io.corbel.iam.model.User;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.iam.utils.Message;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.TokenInfo.Builder;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;

import java.security.SignatureException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander De Leon
 */
public class DefaultAuthorizationService implements AuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthorizationService.class);
    private final JsonTokenParser jsonTokenParser;
    private final List<AuthorizationRule> rules;
    private final TokenFactory tokenFactory;
    private final AuthorizationRequestContextFactory contextFactory;
    private final ScopeService scopeService;
    private final AuthorizationProviderFactory authorizationProviderFactory;
    private final RefreshTokenService refreshTokenService;
    private final UserTokenRepository userTokenRepository;
    private final UserService userService;
    private final EventsService eventsService;

    public DefaultAuthorizationService(JsonTokenParser jsonTokenParser, List<AuthorizationRule> rules, TokenFactory accessTokenFactory,
            AuthorizationRequestContextFactory contextFactory, ScopeService scopeService,
            AuthorizationProviderFactory authorizationProviderFactory, RefreshTokenService refreshTokenService,
            UserTokenRepository userTokenRepository, UserService userService, EventsService eventsService) {
        this.jsonTokenParser = jsonTokenParser;
        this.eventsService = eventsService;
        this.rules = Collections.unmodifiableList(rules);
        this.tokenFactory = accessTokenFactory;
        this.contextFactory = contextFactory;
        this.scopeService = scopeService;
        this.authorizationProviderFactory = authorizationProviderFactory;
        this.refreshTokenService = refreshTokenService;
        this.userTokenRepository = userTokenRepository;
        this.userService = userService;
    }

    @Override
    public TokenGrant authorize(String assertion) throws UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        TokenGrant tokenGrant;
        try {
            AuthorizationRequestContext context = getContext(assertion);
            if (context.isOAuth()) {
                OauthParams params = context.getOauthParams();
                checkOauthParams(context, params);
                tokenGrant = grantAccess(context, params);
            } else if (context.isBasic()) {
                BasicParams params = context.getBasicParams();
                checkBasicParams(context, params);
                tokenGrant = grantAccess(context, params);
            } else if (context.hasRefreshToken()) {
                tokenGrant = refreshToken(context);
            } else {
                tokenGrant = grantAccess(context, Optional.ofNullable(context.getPrincipal()));
            }
            return tokenGrant;
        } catch (SignatureException | TokenVerificationException e) {
            throw new UnauthorizedException(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            logInvalidAssertion(assertion, e);
            throw new UnauthorizedException("Invalid assertion");
        }
    }


    @Override
    public TokenGrant authorize(String assertion, OauthParams params) throws UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException {
        try {
            AuthorizationRequestContext context = getContext(assertion);
            checkOauthParams(context, params);
            return grantAccess(context, params);
        } catch (SignatureException e) {
            throw new UnauthorizedException(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            logInvalidAssertion(assertion, e);
            throw new UnauthorizedException("Invalid assertion");
        }
    }

    private void checkOauthParams(AuthorizationRequestContext context, OauthParams params) throws MissingOAuthParamsException {
        if (!context.isOAuth() || params == null || params.isMissing()) {
            throw new MissingOAuthParamsException("Missing oauth params");
        }
    }

    private void checkBasicParams(AuthorizationRequestContext context, BasicParams params) throws MissingBasicParamsException {
        if (!context.isBasic() || params == null || params.isMissing()) {
            throw new MissingBasicParamsException("Missing basic params");
        }
    }

    private void logInvalidAssertion(String assertion, RuntimeException e) {
        LOG.warn("Invalid JWT: {}. Reason {}:{}", assertion, e.getClass().getCanonicalName(), e.getMessage());
    }

    private TokenGrant grantAccess(AuthorizationRequestContext context, OauthParams oauthParams) throws SignatureException,
            UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        Domain domain = context.getRequestedDomain();
        String oAuthService = context.getOAuthService();
        Provider provider = authorizationProviderFactory.getProvider(domain, oAuthService);

        Optional<Identity> identity;
        try {
            identity = Optional.ofNullable(provider.getIdentity(oauthParams, oAuthService, domain.getId()));
        } catch (MissingOAuthParamsException | UnauthorizedException | OauthServerConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException(e.getMessage());
        }

        Optional<User> user = identity.map(theIdentity -> userService.findById(theIdentity.getUserId()));

        if (user.isPresent()) {
            context.setPrincipalId(user.get().getUsername());
            return grantAccess(context, user);
        } else {
            throw new NoSuchPrincipalException(Message.OAUTH_PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(oAuthService, context
                    .getIssuerClient().getDomain()));
        }
    }

    private TokenGrant grantAccess(AuthorizationRequestContext context, BasicParams basicParams) throws UnauthorizedException {
        Domain domain = context.getRequestedDomain();

        User user;
        user = userService.findByDomainAndUsername(domain.getId(), basicParams.getUsername());
        if (user == null) {
            user = userService.findByDomainAndEmail(domain.getId(), basicParams.getUsername());
        }
        if (user != null && user.checkPassword(basicParams.getPassword())) {
            context.setPrincipalId(user.getUsername());
            return grantAccess(context, Optional.of(user));
        } else {
            throw new NoSuchPrincipalException(Message.UNKNOWN_BASIC_USER_CREDENTIALS.getMessage());
        }
    }

    private TokenGrant grantAccess(AuthorizationRequestContext context, Optional<User> user) throws UnauthorizedException {
        for (AuthorizationRule processor : rules) {
            processor.process(context);
        }
        String accessToken = getAccessToken(context);
        TokenGrant tokenGrant = new TokenGrant(accessToken, context.getAuthorizationExpiration(), refreshTokenService.createRefreshToken(
                context, accessToken));

        if (user.isPresent()) {
            storeUserToken(tokenGrant, user.get());
            eventsService.sendUserAuthenticationEvent(user.get().getUserProfile());
        } else {
            eventsService.sendClientAuthenticationEvent(context.getIssuerClientDomain().getId(), context.getIssuerClientId());
        }

        publishScope(tokenGrant, context);
        return tokenGrant;
    }

    private void publishScope(TokenGrant tokenGrant, AuthorizationRequestContext context) {
        Set<Scope> expandedRequestedScopes = context.getExpandedRequestedScopes();
        String principalId = context.hasPrincipal() ? context.getPrincipal().getId() : null;
        String issuerClientId = context.getIssuerClientId();
        String domainId = context.getRequestedDomain().getId();
        Set<Scope> filledScopes = scopeService.fillScopes(expandedRequestedScopes, principalId, issuerClientId, domainId);
        scopeService.publishAuthorizationRules(tokenGrant.getAccessToken(), tokenGrant.getExpiresAt(), filledScopes);
    }

    private AuthorizationRequestContext getContext(String assertion) throws SignatureException, UnauthorizedTimeException {
        try {
            JsonToken jwt = jsonTokenParser.verifyAndDeserialize(assertion);
            return contextFactory.fromJsonToken(jwt);
        } catch (IllegalStateException e) {
            if (isAnExpiredException(assertion)) {
                throw new UnauthorizedTimeException(e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private boolean isAnExpiredException(String assertion) {
        JsonToken unverifiedToken = jsonTokenParser.deserialize(assertion);
        Instant instantNow = Instant.now();
        return !(jsonTokenParser.issuedAtIsValid(unverifiedToken, instantNow) && jsonTokenParser.expirationIsValid(unverifiedToken,
                instantNow));
    }

    private String getAccessToken(AuthorizationRequestContext context) {
        Builder tokenBuilder = TokenInfo.newBuilder().setType(TokenType.TOKEN).setClientId(context.getIssuerClient().getId())
                .setState(Long.toString(context.getAuthorizationExpiration())).setDomainId(context.getRequestedDomain().getId());
        Optional.ofNullable(context.getDeviceId()).ifPresent(deviceId -> tokenBuilder.setDeviceId(deviceId));
        TokenInfo tokenInfo = context.hasPrincipal() ? tokenBuilder.setUserId(context.getPrincipal().getId())
                .setGroups(context.getPrincipal().getGroups()).build() : tokenBuilder.build();
        return tokenFactory.createToken(tokenInfo, context.getAuthorizationExpiration()).getAccessToken();
    }

    private TokenGrant refreshToken(AuthorizationRequestContext context) throws TokenVerificationException, UnauthorizedException {
        User user = refreshTokenService.getUserFromRefreshToken(context.getRefreshToken());
        if (user == null) {
            throw new UnauthorizedException(Message.PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(context.getPrincipalId()));
        }
        context.setPrincipalId(user.getUsername());
        return grantAccess(context, Optional.of(user));
    }

    private void storeUserToken(TokenGrant tokenGrant, User user) {
        UserToken userToken = new UserToken(tokenGrant.getAccessToken(), user.getId(), new Date(tokenGrant.getExpiresAt()));
        userTokenRepository.save(userToken);
    }

}
