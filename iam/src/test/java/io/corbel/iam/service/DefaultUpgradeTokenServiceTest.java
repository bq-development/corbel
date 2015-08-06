package io.corbel.iam.service;

import static org.mockito.Mockito.*;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.auth.AuthorizationRequestContextFactory;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Scope;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@RunWith(MockitoJUnitRunner.class) public class DefaultUpgradeTokenServiceTest {

    private static final String TEST_ASSERTION = "123.456.789";
    private static final String TEST_TOKEN = "the_access_token";
    private static final String TEST_CLIENT = "client";
    private static final String TEST_USER = "user";
    private static final String TEST_SCOPES = "SCOPE_1 SCOPE_2";

    @Mock private JsonTokenParser jsonTokenParser;
    @Mock private AuthorizationRequestContextFactory contextFactory;
    @Mock private ScopeService scopeServiceMock;
    @Mock private TokenInfo accessToken;
    @Mock private TokenReader tokenReader;

    private UpgradeTokenService upgradeTokenService;

    @Before
    public void setUp() {
        upgradeTokenService = new DefaultUpgradeTokenService(jsonTokenParser, scopeServiceMock);
        when(accessToken.getClientId()).thenReturn(TEST_CLIENT);
        when(accessToken.getUserId()).thenReturn(TEST_USER);
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

        when(scopeServiceMock.expandScopes(scopesIds)).thenReturn(scopes);
        when(scopeServiceMock.fillScopes(scopes, TEST_USER, TEST_CLIENT)).thenReturn(scopes);

        when(jsonTokenParser.verifyAndDeserialize(TEST_ASSERTION)).thenReturn(validJsonToken);
        upgradeTokenService.upgradeToken(TEST_ASSERTION, tokenReader);

        verify(scopeServiceMock).fillScopes(scopes, TEST_USER, TEST_CLIENT);
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
        upgradeTokenService.upgradeToken(TEST_ASSERTION, tokenReader);
    }

    @Test
    public void upgradeTokenEmptyScopeTest() throws SignatureException, UnauthorizedException {
        Set<Scope> scopes = new HashSet<>();
        JsonToken validJsonToken = mock(JsonToken.class);
        JsonObject json = new JsonObject();
        json.add("scope", new JsonPrimitive(""));
        when(validJsonToken.getPayloadAsJsonObject()).thenReturn(json);
        when(jsonTokenParser.verifyAndDeserialize(TEST_ASSERTION)).thenReturn(validJsonToken);
        upgradeTokenService.upgradeToken(TEST_ASSERTION, tokenReader);

        verify(scopeServiceMock).fillScopes(scopes, TEST_USER, TEST_CLIENT);
        verify(scopeServiceMock).addAuthorizationRules(TEST_TOKEN, scopes);
    }
}
