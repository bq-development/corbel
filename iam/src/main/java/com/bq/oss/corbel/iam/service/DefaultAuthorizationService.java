package com.bq.oss.corbel.iam.service;

import java.security.SignatureException;
import java.util.*;
import java.util.function.BiFunction;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.iam.auth.*;
import com.bq.oss.corbel.iam.auth.provider.AuthorizationProviderFactory;
import com.bq.oss.corbel.iam.auth.provider.Provider;
import com.bq.oss.corbel.iam.exception.*;
import com.bq.oss.corbel.iam.model.*;
import com.bq.oss.corbel.iam.repository.UserTokenRepository;
import com.bq.oss.corbel.iam.utils.Message;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.TokenInfo.Builder;
import com.bq.oss.lib.token.exception.TokenVerificationException;
import com.bq.oss.lib.token.factory.TokenFactory;
import com.bq.oss.lib.token.model.TokenType;

/**
 * @author Alexander De Leon
 * 
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

    public DefaultAuthorizationService(JsonTokenParser jsonTokenParser, List<AuthorizationRule> rules, TokenFactory accessTokenFactory,
            AuthorizationRequestContextFactory contextFactory, ScopeService scopeService,
            AuthorizationProviderFactory authorizationProviderFactory, RefreshTokenService refreshTokenService,
            UserTokenRepository userTokenRepository, UserService userService) {
        this.jsonTokenParser = jsonTokenParser;
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
                tokenGrant = grantAccess(context);
            }
        } catch (SignatureException | TokenVerificationException e) {
            throw new UnauthorizedException(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            logInvalidAssertion(assertion, e);
            throw new UnauthorizedException(getMessageByException(e));
        }
        return tokenGrant;
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
            throw new UnauthorizedException(getMessageByException(e));
        }
    }

    private String getMessageByException(RuntimeException e) {
        return (String) Optional.ofNullable(e.getMessage()).map(error -> {
            if (error.contains("Invalid iat and/or exp.")) {
                return "Authorization request is now past. Check your system clock.";
            }
            return null;
        }).orElse("Invalid assertion");


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

    private TokenGrant grantAccess(AuthorizationRequestContext context, OauthParams OauthParams) throws SignatureException,
            UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException {
        Domain domain = context.getRequestedDomain();
        String oAuthService = context.getOAuthService();
        Provider provider = authorizationProviderFactory.getProvider(domain, oAuthService);

        Optional<Identity> identity = Optional.empty();
        try {
            identity = Optional.ofNullable(provider.getIdentity(OauthParams, oAuthService, domain.getId()));
        } catch (MissingOAuthParamsException | UnauthorizedException | OauthServerConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException(e.getMessage());
        }

        Optional<User> user = identity.map(theIdentity -> userService.findById(theIdentity.getUserId()));

        if (user.isPresent()) {
            context.setPrincipalId(user.get().getUsername());
            return grantAccess(context);
        } else {
            throw new NoSuchPrincipalException(Message.OAUTH_PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(oAuthService, context
                    .getIssuerClient().getDomain()));
        }
    }

    private TokenGrant grantAccess(AuthorizationRequestContext context, BasicParams basicParams) throws UnauthorizedException {
        Domain domain = context.getRequestedDomain();

        List<BiFunction<String, String, User>> funcList = Arrays.asList(userService::findByDomainAndUsername,
                userService::findByDomainAndEmail);

        for (BiFunction<String, String, User> func : funcList) {
            Optional<User> candidateUser = Optional.ofNullable(func.apply(domain.getId(), basicParams.getUsername())).map(user -> {
                if (user.checkPassword(basicParams.getPassword()))
                    return user;
                else
                    return null;
            });

            if (candidateUser.isPresent()) {
                context.setPrincipalId(candidateUser.get().getUsername());
                return grantAccess(context);
            }
        }

        throw new NoSuchPrincipalException(Message.UNKNOWN_BASIC_USER_CREDENTIALS.getMessage());
    }

    private TokenGrant grantAccess(AuthorizationRequestContext context) throws UnauthorizedException {
        for (AuthorizationRule processor : rules) {
            processor.process(context);
        }
        String accessToken = getAccessToken(context);
        TokenGrant tokenGrant = new TokenGrant(accessToken, context.getAuthorizationExpiration(), refreshTokenService.createRefreshToken(
                context, accessToken));

        if (context.hasPrincipal()) {
            storeUserToken(tokenGrant, context);
        }

        publishScope(tokenGrant, context);
        return tokenGrant;
    }

    private void publishScope(TokenGrant tokenGrant, AuthorizationRequestContext context) {
        Set<Scope> expandedRequestedScopes = context.getExpandedRequestedScopes();
        String principalId = context.hasPrincipal() ? context.getPrincipal().getId() : null;
        String issuerClientId = context.getIssuerClientId();
        Set<Scope> filledScopes = scopeService.fillScopes(expandedRequestedScopes, principalId, issuerClientId);
        scopeService.publishAuthorizationRules(tokenGrant.getAccessToken(), tokenGrant.getExpiresAt(), filledScopes);
    }

    private AuthorizationRequestContext getContext(String assertion) throws SignatureException {
        JsonToken jwt = jsonTokenParser.verifyAndDeserialize(assertion);
        return contextFactory.fromJsonToken(jwt);
    }

    private String getAccessToken(AuthorizationRequestContext context) {
        Builder tokenBuilder = TokenInfo.newBuilder().setType(TokenType.TOKEN).setClientId(context.getIssuerClient().getId())
                .setState(Long.toString(context.getAuthorizationExpiration())).setDomainId(context.getRequestedDomain().getId());
        Optional.ofNullable(context.getDeviceId()).ifPresent(deviceId -> tokenBuilder.setDeviceId(deviceId));
        TokenInfo tokenInfo = context.hasPrincipal() ? tokenBuilder.setUserId(context.getPrincipal().getId()).build() : tokenBuilder
                .build();
        return tokenFactory.createToken(tokenInfo, context.getAuthorizationExpiration()).getAccessToken();
    }

    private TokenGrant refreshToken(AuthorizationRequestContext context) throws TokenVerificationException, UnauthorizedException {
        User user = refreshTokenService.getUserFromRefreshToken(context.getRefreshToken());
        if (user == null) {
            throw new UnauthorizedException(Message.PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(context.getPrincipalId()));
        }
        context.setPrincipalId(user.getUsername());

        return grantAccess(context);
    }

    private void storeUserToken(TokenGrant tokenGrant, AuthorizationRequestContext context) {
        UserToken userToken = new UserToken(tokenGrant.getAccessToken(), context.getPrincipal().getId(),
                new Date(tokenGrant.getExpiresAt()));
        userTokenRepository.save(userToken);
    }

}
