package io.corbel.iam.service;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.corbel.iam.auth.AuthorizationRequestContextFactory;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Scope;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class) public class DefaultUpgradeTokenServiceTest {

    private static final String TEST_ASSERTION = "123.456.789";
    private static final String TEST_TOKEN = "the_access_token";
    private static final String TEST_CLIENT = "client";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_USER = "user";
    private static final String TEST_SCOPES = "SCOPE_1 SCOPE_2";

    @Mock private JsonTokenParser jsonTokenParser;
    @Mock private AuthorizationRequestContextFactory contextFactory;
    @Mock private ScopeService scopeServiceMock;
    @Mock private TokenInfo accessToken;
    @Mock private TokenReader tokenReader;
    @Mock private UserTokenRepository userTokenRepositoryMock;

    private UpgradeTokenService upgradeTokenService;

    @Before
    public void setUp() {
        upgradeTokenService = new DefaultUpgradeTokenService(jsonTokenParser, scopeServiceMock, userTokenRepositoryMock);
        when(accessToken.getClientId()).thenReturn(TEST_CLIENT);
        when(accessToken.getUserId()).thenReturn(TEST_USER);
        when(accessToken.getDomainId()).thenReturn(TEST_DOMAIN);
        when(accessToken.toString()).thenReturn(TEST_TOKEN);
        when(tokenReader.getInfo()).thenReturn(accessToken);
        when(tokenReader.getToken()).thenReturn(TEST_TOKEN);
    }

    @Test
    public void upgradeTokenTest() throws SignatureException, UnauthorizedException {
        JsonToken validJsonToken = mock(JsonToken.class);
        JsonObject json = new JsonObject();
        json.add("scope", new JsonPrimitive(TEST_SCOPES));
        when(validJsonToken.getPayloadAsJsonObject()).thenReturn(json);
        Scope scope1 = mock(Scope.class);
        Scope scope2 = mock(Scope.class);
        Set<Scope> scopes = new HashSet<Scope>(Arrays.asList(scope1, scope2));
        Set<String> scopesIds = new HashSet<String>(Arrays.asList("SCOPE_1", "SCOPE_2"));
        UserToken userToken = new UserToken();
        userToken.setScopes(new HashSet<>());

        when(scopeServiceMock.expandScopes(scopesIds)).thenReturn(scopes);
        when(scopeServiceMock.fillScopes(scopes, TEST_USER, TEST_CLIENT, TEST_DOMAIN)).thenReturn(scopes);
        when(userTokenRepositoryMock.findByToken(TEST_TOKEN)).thenReturn(userToken);

        when(jsonTokenParser.verifyAndDeserialize(TEST_ASSERTION)).thenReturn(validJsonToken);

        Set<String> scopesToAdd = upgradeTokenService.getScopesFromTokenToUpgrade(TEST_ASSERTION);
        upgradeTokenService.upgradeToken(TEST_ASSERTION, tokenReader, scopesToAdd);

        verify(scopeServiceMock).fillScopes(scopes, TEST_USER, TEST_CLIENT, TEST_DOMAIN);
        verify(scopeServiceMock).addAuthorizationRules(TEST_TOKEN, scopes);
    }

    @Test(expected = UnauthorizedException.class)
    public void upgradeTokenNonexistentScopeTest() throws SignatureException, UnauthorizedException {
        JsonToken validJsonToken = mock(JsonToken.class);
        JsonObject json = new JsonObject();
        json.add("scope", new JsonPrimitive(TEST_SCOPES));
        when(validJsonToken.getPayloadAsJsonObject()).thenReturn(json);

        doThrow(new IllegalStateException("Nonexistent scope scopeId")).when(scopeServiceMock).addAuthorizationRules(anyString(), anySet());

        when(jsonTokenParser.verifyAndDeserialize(TEST_ASSERTION)).thenReturn(validJsonToken);

        Set<String> scopes = upgradeTokenService.getScopesFromTokenToUpgrade(TEST_ASSERTION);
        upgradeTokenService.upgradeToken(TEST_ASSERTION, tokenReader, scopes);
    }

    @Test
    public void upgradeTokenEmptyScopeTest() throws SignatureException, UnauthorizedException {
        Set<Scope> scopes = new HashSet<>();
        JsonToken validJsonToken = mock(JsonToken.class);
        JsonObject json = new JsonObject();
        json.add("scope", new JsonPrimitive(""));
        UserToken userToken = new UserToken();
        userToken.setScopes(new HashSet<>());
        when(validJsonToken.getPayloadAsJsonObject()).thenReturn(json);
        when(jsonTokenParser.verifyAndDeserialize(TEST_ASSERTION)).thenReturn(validJsonToken);
        when(userTokenRepositoryMock.findByToken(TEST_TOKEN)).thenReturn(userToken);
        when(scopeServiceMock.fillScopes(any(), any(), any(), any())).thenReturn(Sets.newHashSet());

        Set<String> scopesToAdd = upgradeTokenService.getScopesFromTokenToUpgrade(TEST_ASSERTION);
        upgradeTokenService.upgradeToken(TEST_ASSERTION, tokenReader, scopesToAdd);

        verify(scopeServiceMock).fillScopes(scopes, TEST_USER, TEST_CLIENT, TEST_DOMAIN);
        verify(scopeServiceMock).addAuthorizationRules(TEST_TOKEN, scopes);
    }
}
