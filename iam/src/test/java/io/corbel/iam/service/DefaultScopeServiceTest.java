package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.corbel.iam.exception.ScopeAbsentIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.corbel.iam.exception.ScopeNameException;
import io.corbel.iam.model.Scope;
import io.corbel.iam.repository.ScopeRepository;
import io.corbel.iam.scope.ScopeFillStrategy;
import io.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;

/**
 * @author Alexander De Leon
 * 
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultScopeServiceTest {

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final long TEST_TOKEN_EXPIRATION_TIME = System.currentTimeMillis() + 36000;
    private static final String TEST_TOKEN = "token";
    private static final String MODULE_A = "moduleA";
    private static final Set<JsonObject> RULES_1 = new HashSet<>(Collections.singletonList(new JsonObject()));
    private static final String MODULE_B = "moduleB";
    private static final Set<JsonObject> RULES_2 = new HashSet<>(Collections.singletonList(new JsonObject()));
    private static final String TEST_USER_ID = "the_user";
    private static final String TEST_CLIENT_ID = "the_client";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_SCOPE_1 = "test_scope1";
    private static final String TEST_SCOPE_2 = "test_scope2";
    private static final String TEST_COMPOSITE_SCOPE = "test_composite_scope";
    private static final String IAM_AUDIENCE = "iamAudience";
    private static final String CUSTOM_PARAM_VALUE = "custom";
    private static final String TEST_ID_CUSTOM = ";testId="+ CUSTOM_PARAM_VALUE;
    private static final String TEST_SCOPE_1_WITH_PARAMS = "test_scope1;testId=" + CUSTOM_PARAM_VALUE;
    private static final String TEST_SCOPE_1_WITH_PARAMS_AND_ERRORS = "test_scope1;error;testId=" + CUSTOM_PARAM_VALUE;
    private static final String TEST_SCOPE_1_WITHOUT_PARAMS = "test_scope1";
    private static final String TEST_SCOPE_1_WITH_NOT_EXIST_PARAMS = "test_scope1;testId=123456";
    private static final JsonObject RULE_WITH_PARAMS = JSON_PARSER.parse("{\"uri\" : \"{{testId}}\"}").getAsJsonObject();
    private static final JsonObject RULE_PARAMS = JSON_PARSER.parse("{\"testId\" : \"cus.*\"}").getAsJsonObject();
    private static final JsonObject RULE_WITH_PARAMS_FILLED = JSON_PARSER.parse("{\"testId\" : \"custom\"}").getAsJsonObject();
    private static final Set<JsonObject> RULES_3 = new HashSet<>(Collections.singletonList(RULE_WITH_PARAMS));
    private final Instant now = Instant.now();
    private DefaultScopeService defaultScopeService;

    @Mock private ScopeRepository scopeRepositoryMock;
    @Mock private AuthorizationRulesRepository authorizationRulesRepositoryMock;
    @Mock private ScopeFillStrategy fillStrategyMock;
    @Mock private EventsService eventsServiceMock;

    @Before
    public void setup() {
        defaultScopeService = new DefaultScopeService(scopeRepositoryMock, authorizationRulesRepositoryMock, 1000L, fillStrategyMock,
                IAM_AUDIENCE, Clock.fixed(now, ZoneId.systemDefault()), eventsServiceMock);
    }

    @Test(expected = NullPointerException.class)
    public void testPublishAuthorizationRulesIllegalArgument() {
        defaultScopeService.publishAuthorizationRules(TEST_TOKEN, TEST_TOKEN_EXPIRATION_TIME, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPublishAuthorizationRules() {

        Scope scope1 = mock(Scope.class);
        when(scope1.getId()).thenReturn("TOKEN_1");
        when(scope1.getAudience()).thenReturn(MODULE_A);
        when(scope1.getRules()).thenReturn(RULES_1);
        when(scope1.isComposed()).thenReturn(false);

        Scope scope2 = mock(Scope.class);
        when(scope2.getId()).thenReturn("TOKEN_2");
        when(scope2.getAudience()).thenReturn(MODULE_B);
        when(scope2.getRules()).thenReturn(RULES_2);
        when(scope2.isComposed()).thenReturn(false);

        Set<Scope> scopes = new HashSet<>(Arrays.asList(scope1, scope2));

        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());

        when(scopeRepositoryMock.findOne(Mockito.eq("TOKEN_1"))).thenReturn(scope1);
        when(scopeRepositoryMock.findOne(Mockito.eq("TOKEN_2"))).thenReturn(scope2);

        when(authorizationRulesRepositoryMock.getKeyForAuthorizationRules(TEST_TOKEN, MODULE_A)).thenReturn(key(TEST_TOKEN, MODULE_A));
        when(authorizationRulesRepositoryMock.getKeyForAuthorizationRules(TEST_TOKEN, MODULE_B)).thenReturn(key(TEST_TOKEN, MODULE_B));

        defaultScopeService.publishAuthorizationRules(TEST_TOKEN, TEST_TOKEN_EXPIRATION_TIME, scopes);

        verify(authorizationRulesRepositoryMock).save(key(TEST_TOKEN, MODULE_A), TEST_TOKEN_EXPIRATION_TIME - now.toEpochMilli(),
                array(RULES_1));
        verify(authorizationRulesRepositoryMock).save(key(TEST_TOKEN, MODULE_B), TEST_TOKEN_EXPIRATION_TIME - now.toEpochMilli(),
                array(RULES_2));
    }

    @Test
    public void testGetScope() {
        String id = "scope_id";
        Scope expectedScope = mock(Scope.class);
        when(scopeRepositoryMock.findOne(id)).thenReturn(expectedScope);
        assertThat(defaultScopeService.getScope(id)).isSameAs(expectedScope);

    }

    @Test
    public void testFillScope() {
        Scope scope1 = mock(Scope.class);
        when(scope1.getAudience()).thenReturn(MODULE_A);
        when(scope1.getRules()).thenReturn(RULES_1);
        defaultScopeService.fillScope(scope1, TEST_USER_ID, TEST_CLIENT_ID, TEST_DOMAIN);
        Map<String, String> params = new HashMap<>();
        params.put("userId", TEST_USER_ID);
        params.put("clientId", TEST_CLIENT_ID);
        params.put("domainId", TEST_DOMAIN);
        verify(fillStrategyMock).fillScope(Mockito.same(scope1), Mockito.eq(params));
    }

    @Test
    public void testFillScopeWithCustomParameters() {
        Scope scope1 = mock(Scope.class);
        when(scope1.getAudience()).thenReturn(MODULE_A);
        when(scope1.getRules()).thenReturn(RULES_3);
        when(scope1.getParameters()).thenReturn(RULE_WITH_PARAMS_FILLED);
        defaultScopeService.fillScope(scope1, TEST_USER_ID, TEST_CLIENT_ID, TEST_DOMAIN);
        Map<String, String> params = new HashMap<>();
        params.put("userId", TEST_USER_ID);
        params.put("clientId", TEST_CLIENT_ID);
        params.put("domainId", TEST_DOMAIN);
        params.put("testId", "custom");
        verify(fillStrategyMock).fillScope(Mockito.same(scope1), Mockito.eq(params));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddAuthorizationRules() {

        Scope scope1 = mock(Scope.class);
        when(scope1.getId()).thenReturn("TOKEN_1");
        when(scope1.getAudience()).thenReturn(MODULE_A);
        when(scope1.getRules()).thenReturn(RULES_1);
        when(scope1.isComposed()).thenReturn(false);

        Scope scope2 = mock(Scope.class);
        when(scope2.getId()).thenReturn("TOKEN_2");
        when(scope2.getAudience()).thenReturn(MODULE_B);
        when(scope2.getRules()).thenReturn(RULES_2);
        when(scope2.isComposed()).thenReturn(false);

        Set<Scope> scopes = new HashSet<>(Arrays.asList(scope1, scope2));

        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());

        when(scopeRepositoryMock.findOne(Mockito.eq("TOKEN_1"))).thenReturn(scope1);
        when(scopeRepositoryMock.findOne(Mockito.eq("TOKEN_2"))).thenReturn(scope2);

        when(authorizationRulesRepositoryMock.getKeyForAuthorizationRules(TEST_TOKEN, MODULE_A)).thenReturn(key(TEST_TOKEN, MODULE_A));
        when(authorizationRulesRepositoryMock.getKeyForAuthorizationRules(TEST_TOKEN, MODULE_B)).thenReturn(key(TEST_TOKEN, MODULE_B));
        when(authorizationRulesRepositoryMock.getKeyForAuthorizationRules(TEST_TOKEN, IAM_AUDIENCE)).thenReturn(
                key(TEST_TOKEN, IAM_AUDIENCE));
        when(authorizationRulesRepositoryMock.getTimeToExpire(key(TEST_TOKEN, IAM_AUDIENCE))).thenReturn(TEST_TOKEN_EXPIRATION_TIME);

        when(authorizationRulesRepositoryMock.existsRules(key(TEST_TOKEN, MODULE_A))).thenReturn(true);
        when(authorizationRulesRepositoryMock.existsRules(key(TEST_TOKEN, MODULE_B))).thenReturn(false);

        defaultScopeService.addAuthorizationRules(TEST_TOKEN, scopes);

        verify(authorizationRulesRepositoryMock).addRules(key(TEST_TOKEN, MODULE_A), array(RULES_1));
        verify(authorizationRulesRepositoryMock).save(key(TEST_TOKEN, MODULE_B), TimeUnit.SECONDS.toMillis(TEST_TOKEN_EXPIRATION_TIME),
                array(RULES_2));
    }

    private String key(String token, String audience) {
        return token + "|" + audience;
    }

    private JsonObject[] array(Set<JsonObject> rules) {
        return rules.toArray(new JsonObject[rules.size()]);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testComposedScopes() {
        Set<String> requestScopes = new HashSet<>(Collections.singletonList(TEST_COMPOSITE_SCOPE));

        Scope scope1 = new Scope(TEST_SCOPE_1, null, IAM_AUDIENCE, null, RULES_3, null);
        Scope scope2 = new Scope(TEST_SCOPE_2, null, IAM_AUDIENCE, null, RULES_3, null);
        HashSet<String> scopesFromCompositScopes = new HashSet<>(Arrays.asList(TEST_COMPOSITE_SCOPE, TEST_SCOPE_1, TEST_SCOPE_2,
                TEST_COMPOSITE_SCOPE));
        Scope compositeScope = new Scope(TEST_COMPOSITE_SCOPE, Scope.COMPOSITE_SCOPE_TYPE, IAM_AUDIENCE, scopesFromCompositScopes, null,
                null);

        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_COMPOSITE_SCOPE))).thenReturn(compositeScope);
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_1))).thenReturn(scope1);
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_2))).thenReturn(scope2);

        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());

        Set<Scope> expandedScopes = defaultScopeService.expandScopes(requestScopes);

        assertThat(expandedScopes).contains(scope1);
        assertThat(expandedScopes).contains(scope2);
        assertThat(expandedScopes).doesNotContain(compositeScope);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testComposedScopesWithParam() {
        Set<String> requestScopes = new HashSet<>(Collections.singletonList(TEST_COMPOSITE_SCOPE + TEST_ID_CUSTOM));

        Scope scope1 = new Scope(TEST_SCOPE_1, null, IAM_AUDIENCE, null, RULES_3, RULE_PARAMS);
        Scope scope2 = new Scope(TEST_SCOPE_2, null, IAM_AUDIENCE, null, RULES_3, null);
        HashSet<String> scopesFromCompositScopes = new HashSet<>(Arrays.asList(TEST_SCOPE_1, TEST_SCOPE_2));
        Scope compositeScope = new Scope(TEST_COMPOSITE_SCOPE, Scope.COMPOSITE_SCOPE_TYPE, IAM_AUDIENCE, scopesFromCompositScopes, null,
                RULE_PARAMS);

        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_COMPOSITE_SCOPE))).thenReturn(compositeScope);
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_1))).thenReturn(scope1);
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_2))).thenReturn(scope2);

        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());

        Set<Scope> expandedScopes = defaultScopeService.expandScopes(requestScopes);

        assertThat(expandedScopes).contains(scope2);
        Scope res = expandedScopes.iterator().next();
        assertThat(res.getParameters()).isEqualTo(RULE_WITH_PARAMS_FILLED);
        assertThat(scope1.getParameters()).isNotEqualTo(res.getParameters());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScopeWithCustomParameters() {
        Scope scope1 = new Scope(TEST_SCOPE_1, null, IAM_AUDIENCE, null, RULES_3, RULE_PARAMS);

        Set<String> requestScopes = new HashSet<>(Collections.singletonList(TEST_SCOPE_1_WITH_PARAMS));
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_1))).thenReturn(scope1);
        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());
        Set<Scope> scopes = defaultScopeService.getScopes(requestScopes);
        Scope scope2 = scopes.iterator().next();
        assertThat(scope2.getParameters()).isEqualTo(RULE_WITH_PARAMS_FILLED);
        assertThat(scope1.getParameters()).isNotEqualTo(scope2.getParameters());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScopeWithErrorsInCustomParameters() {
        Scope scope1 = new Scope(TEST_SCOPE_1, null, IAM_AUDIENCE, null, RULES_3, RULE_PARAMS);

        Set<String> requestScopes = new HashSet<>(Collections.singletonList(TEST_SCOPE_1_WITH_PARAMS_AND_ERRORS));
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_1))).thenReturn(scope1);
        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());
        Set<Scope> scopes = defaultScopeService.getScopes(requestScopes);
        assertThat(scopes.iterator().next().getParameters()).isEqualTo(RULE_WITH_PARAMS_FILLED);
    }

    @SuppressWarnings("unchecked")
    public void testScopeWithoutCustomParametersDefined() {
        Scope scope1 = new Scope(TEST_SCOPE_1, null, IAM_AUDIENCE, null, RULES_3, RULE_PARAMS);

        Set<String> requestScopes = new HashSet<>(Collections.singletonList(TEST_SCOPE_1_WITHOUT_PARAMS));
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_1))).thenReturn(scope1);
        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());
        Set<Scope> scopes = defaultScopeService.getScopes(requestScopes);
        assertThat(scopes.iterator().next().getParameters()).isEqualTo(null);
    }

    @SuppressWarnings("unchecked")
    public void testScopeWithNotExistCustomParameters() {
        Scope scope1 = new Scope(TEST_SCOPE_1, null, IAM_AUDIENCE, null, RULES_3, RULE_PARAMS);

        Set<String> requestScopes = new HashSet<>(Collections.singletonList(TEST_SCOPE_1_WITH_NOT_EXIST_PARAMS));
        when(scopeRepositoryMock.findOne(Mockito.eq(TEST_SCOPE_1))).thenReturn(scope1);
        doAnswer(returnsFirstArg()).when(fillStrategyMock).fillScope(Matchers.<Scope>any(), anyMap());
        Set<Scope> scopes = defaultScopeService.getScopes(requestScopes);
        assertThat(scopes.iterator().next().getParameters()).isEqualTo(null);
    }

    @Test
    public void testCreateScope() throws ScopeNameException, ScopeAbsentIdException {
        Scope scope = mock(Scope.class);
        when(scope.getId()).thenReturn(TEST_SCOPE_1);
        defaultScopeService.create(scope);
        verify(scopeRepositoryMock).save(scope);
    }


    @Test(expected = ScopeNameException.class)
    public void testCreateIncorrectScope() throws ScopeNameException, ScopeAbsentIdException {
        Scope scope = new Scope(";", null, null, null, null, null);
        defaultScopeService.create(scope);
    }

    @Test
    public void testDeleteScope() {
        defaultScopeService.delete(TEST_SCOPE_1);
        verify(scopeRepositoryMock).delete(TEST_SCOPE_1);
    }


}
