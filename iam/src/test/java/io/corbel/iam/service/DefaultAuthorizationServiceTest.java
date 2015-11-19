package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.auth.AuthorizationRequestContextFactory;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.auth.provider.AuthorizationProviderFactory;
import io.corbel.iam.exception.MissingBasicParamsException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.exception.UnauthorizedTimeException;
import io.corbel.iam.model.Client;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Scope;
import io.corbel.iam.model.TokenGrant;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Alexander De Leon
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultAuthorizationServiceTest {

    private static final String TEST_JWT = "1111.2222.3333";

    private static final String TEST_CLIENT_ID = "123";

    private static final Long TEST_EXPIRATION = System.currentTimeMillis();

    private static final String TEST_TOKEN = "the_access_token";

    private static final String TEST_REFRESH_TOKEN = "the_refresh_token";

    private static final String TEST_USER_ID = "the_user";

    private static final String TEST_SCOPE_1 = "test_scope1";

    private static final String TEST_DOMAIN_ID = "test_domain_id";

    private static final Set<String> TEST_USER_GROUPS = new HashSet<String>(Arrays.asList("Admins", "Users"));

    private JsonTokenParser jsonTokenParserMock;
    private DefaultAuthorizationService authorizationService;
    private AuthorizationRule authorizationProcessorMock;
    private TokenInfo tokenInfo;
    private TokenFactory accessTokenFactoryMock;
    private AuthorizationRequestContext contextMock;
    private ScopeService scopeServiceMock;
    private AuthorizationProviderFactory authorizationProviderFactoryMock;
    private RefreshTokenService refreshTokenServiceMock;
    private io.corbel.lib.token.TokenGrant tokenGrant;
    private UserTokenRepository userTokenRepository;
    private EventsService eventsService;

    @Mock private UserService userService;
    private final net.oauth.jsontoken.JsonToken jsonTokenMock = mock(JsonToken.class);

    @Before
    public void setUp() {
        jsonTokenParserMock = mock(JsonTokenParser.class);
        accessTokenFactoryMock = mock(TokenFactory.class);
        authorizationProcessorMock = mock(AuthorizationRule.class);
        contextMock = mock(AuthorizationRequestContext.class);
        scopeServiceMock = mock(ScopeService.class);
        authorizationProviderFactoryMock = mock(AuthorizationProviderFactory.class);
        refreshTokenServiceMock = mock(RefreshTokenService.class);
        userTokenRepository = mock(UserTokenRepository.class);
        eventsService = mock(EventsService.class);

        AuthorizationRequestContextFactory factory = mock(AuthorizationRequestContextFactory.class);
        when(factory.fromJsonToken(Mockito.any(JsonToken.class))).thenReturn(contextMock);
        authorizationService = new DefaultAuthorizationService(jsonTokenParserMock, Arrays.asList(authorizationProcessorMock),
                accessTokenFactoryMock, factory, scopeServiceMock, authorizationProviderFactoryMock, refreshTokenServiceMock,
                userTokenRepository, userService, eventsService);

        tokenGrant = new io.corbel.lib.token.TokenGrant(TEST_TOKEN, TEST_EXPIRATION);

        tokenInfo = TokenInfo.newBuilder().setType(TokenType.TOKEN).setClientId(TEST_CLIENT_ID).setState(Long.toString(TEST_EXPIRATION))
                .setDomainId(TEST_DOMAIN_ID).setUserId(TEST_USER_ID).setGroups(TEST_USER_GROUPS).build();
    }

    @Test
    public void testAuthorized() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        JsonToken validJsonToken = mock(JsonToken.class);
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenReturn(validJsonToken);
        initContext(false);
        TokenInfo tokenInfoWithoutUserId = TokenInfo.newBuilder().setType(TokenType.TOKEN).setClientId(TEST_CLIENT_ID)
                .setState(Long.toString(TEST_EXPIRATION)).setDomainId(TEST_DOMAIN_ID).build();
        when(accessTokenFactoryMock.createToken(tokenInfoWithoutUserId, TEST_EXPIRATION)).thenReturn(tokenGrant);
        when(scopeServiceMock.getScope(TEST_SCOPE_1)).thenReturn(mock(Scope.class));
        TokenGrant grant = authorizationService.authorize(TEST_JWT);
        assertThat(grant).isNotNull();
        verify(eventsService).sendClientAuthenticationEvent(TEST_DOMAIN_ID, TEST_CLIENT_ID);
    }

    @Test
    public void testAuthorizedWithPrincipal() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        Set<Scope> filledScopes = new HashSet();

        JsonToken validJsonToken = mock(JsonToken.class);
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenReturn(validJsonToken);
        initContext(true);
        when(accessTokenFactoryMock.createToken(tokenInfo, TEST_EXPIRATION)).thenReturn(tokenGrant);

        TokenGrant grant = authorizationService.authorize(TEST_JWT);
        verify(scopeServiceMock).publishAuthorizationRules(TEST_TOKEN, TEST_EXPIRATION, filledScopes);
        assertThat(grant).isNotNull();
        verify(eventsService).sendUserAuthenticationEvent(TEST_DOMAIN_ID, TEST_USER_ID);
    }

    @Test
    public void testRefreshToken() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            TokenVerificationException, OauthServerConnectionException, MissingBasicParamsException {
        Set<Scope> filledScopes = new HashSet();

        User userMock = mock(User.class);
        when(userMock.getUsername()).thenReturn(TEST_USER_ID);
        JsonToken validJsonToken = mock(JsonToken.class);
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenReturn(validJsonToken);
        when(contextMock.hasRefreshToken()).thenReturn(true);
        when(contextMock.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
        initContext(true);
        when(accessTokenFactoryMock.createToken(tokenInfo, TEST_EXPIRATION)).thenReturn(tokenGrant);
        when(refreshTokenServiceMock.getUserFromRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(userMock);
        TokenGrant grant = authorizationService.authorize(TEST_JWT);
        verify(scopeServiceMock).publishAuthorizationRules(TEST_TOKEN, TEST_EXPIRATION, filledScopes);
        assertThat(grant).isNotNull();
        verify(eventsService).sendUserAuthenticationEvent(TEST_DOMAIN_ID, TEST_USER_ID);
    }

    @Test(expected = UnauthorizedException.class)
    public void testInvalidSignature() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenThrow(new SignatureException());
        authorizationService.authorize(TEST_JWT);
    }

    @Test(expected = UnauthorizedTimeException.class)
    public void testBadSystemClockException() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(jsonTokenParserMock.deserialize(any())).thenReturn(jsonTokenMock);
        when(jsonTokenMock.getIssuedAt()).thenReturn(new Instant(2));
        when(jsonTokenMock.getExpiration()).thenReturn(new Instant(1));
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenThrow(
                new IllegalStateException("Invalid iat and/or exp. iat: 1900/01/01 exp: 1900/01/01 now: 1900/01/01"));
        authorizationService.authorize(TEST_JWT);
    }

    @Test(expected = UnauthorizedException.class)
    public void testBadToken() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenThrow(new IllegalArgumentException());
        authorizationService.authorize(TEST_JWT);
    }

    @Test(expected = UnauthorizedException.class)
    public void testBadToken2() throws SignatureException, UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException, MissingBasicParamsException {
        when(jsonTokenParserMock.verifyAndDeserialize(TEST_JWT)).thenThrow(new IllegalStateException());
        when(jsonTokenParserMock.issuedAtIsValid(any(), any())).thenReturn(true);
        when(jsonTokenParserMock.expirationIsValid(any(), any())).thenReturn(true);
        authorizationService.authorize(TEST_JWT);
    }

    private void initContext(boolean withPrincipal) {
        Client clientMock = mock(Client.class);
        when(clientMock.getId()).thenReturn(TEST_CLIENT_ID);
        when(contextMock.getIssuerClientId()).thenReturn(TEST_CLIENT_ID);
        if (withPrincipal) {
            User userMock = mock(User.class);
            when(userMock.getId()).thenReturn(TEST_USER_ID);
            when(userMock.getGroups()).thenReturn(TEST_USER_GROUPS);
            when(contextMock.hasPrincipal()).thenReturn(true);
            when(contextMock.getPrincipal()).thenReturn(userMock);
            when(contextMock.getPrincipal(TEST_USER_ID)).thenReturn(userMock);
            when(contextMock.getPrincipalId()).thenReturn(TEST_USER_ID);
        }
        when(contextMock.getIssuerClient()).thenReturn(clientMock);
        when(contextMock.getAuthorizationExpiration()).thenReturn(TEST_EXPIRATION);
        when(contextMock.getRequestedScopes()).thenReturn(Collections.singleton(TEST_SCOPE_1));
        Domain domainMock = mock(Domain.class);
        when(domainMock.getId()).thenReturn(TEST_DOMAIN_ID);
        when(contextMock.getIssuerClientDomain()).thenReturn(domainMock);
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);

        Map<String, Map<String, String>> configurations = new HashMap<>();
        Map<String, String> config = new HashMap<>();
        config.put("clientId", "asdf");
        config.put("clientSecret", "qwer");
        config.put("redirectUri", "http://test.com");
        configurations.put("facebook", config);
        when(domainMock.getAuthConfigurations()).thenReturn(configurations);
    }

}
