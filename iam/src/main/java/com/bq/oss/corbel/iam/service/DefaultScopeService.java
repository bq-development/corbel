package com.bq.oss.corbel.iam.service;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.exception.ScopeNameException;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.model.Entity;
import com.bq.oss.corbel.iam.model.Scope;
import com.bq.oss.corbel.iam.repository.ScopeRepository;
import com.bq.oss.corbel.iam.scope.ScopeFillStrategy;
import com.bq.oss.lib.ws.auth.repository.AuthorizationRulesRepository;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultScopeService implements ScopeService {

    public static final String EXPAND_SCOPES_CACHE = "expandScopesCache";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultScopeService.class);
    private static final int SCOPE_ID_POSITION = 0;
    private static final int FIRST_PARAM_POSITION = 1;

    private final ScopeRepository scopeRepository;
    private final AuthorizationRulesRepository authorizationRulesRepository;
    private final ScopeFillStrategy fillStrategy;
    private final String iamAudience;
    private final Clock clock;

    public DefaultScopeService(ScopeRepository scopeRepository, AuthorizationRulesRepository authorizationRulesRepository,
            ScopeFillStrategy fillStrategy, String iamAudience, Clock clock) {
        this.scopeRepository = scopeRepository;
        this.authorizationRulesRepository = authorizationRulesRepository;
        this.fillStrategy = fillStrategy;
        this.iamAudience = iamAudience;
        this.clock = clock;
    }

    public DefaultScopeService(ScopeRepository scopeRepository, AuthorizationRulesRepository authorizationRulesRepository,
            ScopeFillStrategy fillStrategy, String iamAudience) {
        this(scopeRepository, authorizationRulesRepository, fillStrategy, iamAudience, Clock.systemDefaultZone());
    }

    @Override
    public Scope getScope(String id) {
        return scopeRepository.findOne(id);
    }

    @Override
    public Set<Scope> getScopes(Collection<String> scopes) {
        if (CollectionUtils.isEmpty(scopes)) {
            return Collections.emptySet();
        }
        return getScopes(scopes.toArray(new String[scopes.size()]));
    }

    @Override
    public Set<Scope> getScopes(String... scopes) {
        List<Scope> fetchedScopes = new ArrayList<>();
        if (scopes.length > 0) {
            for (String scopeId : scopes) {
                String[] scopeIdAndParams = scopeId.split(";");
                Optional.ofNullable(getScope(scopeIdAndParams[SCOPE_ID_POSITION])).ifPresent(scope -> {
                    if (scopeHasCustomParameters(scope)) {
                        fillScopeCustomParameters(scope, scopeIdAndParams);
                    }
                    fetchedScopes.add(scope);
                });
            }
            if (fetchedScopes.size() != scopes.length) {
                Set<String> fetchedScopesIds = fetchedScopes.stream().map(Entity::getId).collect(Collectors.toSet());
                throw new IllegalStateException("Nonexistent scope: " + Sets.difference(Sets.newHashSet(scopes), fetchedScopesIds));
            }
        }
        return Sets.newHashSet(fetchedScopes);
    }

    private boolean scopeHasCustomParameters(Scope scope) {
        return scope.getParameters() != null;
    }

    private void fillScopeCustomParameters(Scope scope, String[] scopeIdAndParams) {
        Map<String, String> parameters = createParametersMap(scopeIdAndParams);
        scope.getParameters().entrySet().stream().forEach(entry -> {
            if (parameters.containsKey(entry.getKey())) {
                String value = parameters.get(entry.getKey());
                if (Pattern.matches(entry.getValue().getAsString(), value)) {
                    scope.getParameters().add(entry.getKey(), new JsonPrimitive(value));
                } else {
                    throw new IllegalStateException("Custom parameter " + entry.getKey() + " don't match with any asset parameter");
                }
            } else {
                throw new IllegalStateException("Asset doesn't contain parameter " + entry.getKey() + " value");
            }
        });
    }

    private Map<String, String> createParametersMap(String[] scopeIdAndParams) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("scopeId", scopeIdAndParams[SCOPE_ID_POSITION]);
        for (int currentParameter = FIRST_PARAM_POSITION; currentParameter < scopeIdAndParams.length; currentParameter++) {
            String[] parameter = scopeIdAndParams[currentParameter].split("=");
            if (parameter.length == 2) {
                parameters.put(parameter[0], parameter[1]);
            } else {
                LOG.warn("Custom parameter " + parameter[0] + " is wrong");
            }
        }
        return parameters;
    }

    @Override
    public Scope fillScope(Scope scope, String userId, String clientId) {
        Validate.notNull(scope, "scope must not be null");
        Validate.notNull(clientId, "clientId must not be null");
        Map<String, String> params = createDefaultParams("userId", userId, "clientId", clientId);
        fillParamsWithCustomParameters(scope, params);
        return fillStrategy.fillScope(scope, params);
    }

    private void fillParamsWithCustomParameters(Scope scope, Map<String, String> params) {
        if (scope.getParameters() != null) {
            for (Map.Entry<String, JsonElement> entry : scope.getParameters().entrySet()) {
                params.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
    }

    @Override
    public void publishAuthorizationRules(String token, long tokenExpirationTime, Set<String> scopes, String principalId,
            String issuerClientId) {
        Validate.notNull(scopes);
        Validate.noNullElements(scopes);
        Set<Scope> filledScopes = expandScopes(scopes).stream().map(s -> fillScope(s, principalId, issuerClientId))
                .collect(Collectors.toSet());

        Map<String, Set<JsonObject>> rules = prepareRules(filledScopes.toArray(new Scope[filledScopes.size()]));

        for (Map.Entry<String, Set<JsonObject>> entry : rules.entrySet()) {
            Set<JsonObject> audienceRules = entry.getValue();

            String keyForAuthorizationRules = authorizationRulesRepository.getKeyForAuthorizationRules(token, entry.getKey());
            authorizationRulesRepository.save(keyForAuthorizationRules, getMillisTo(tokenExpirationTime),
                    audienceRules.toArray(new JsonObject[audienceRules.size()]));
        }
    }

    @Override
    public void addAuthorizationRules(String token, Set<String> scopes, String principalId, String issuerClientId) {
        Set<Scope> filledScopes = expandScopes(scopes).stream().map(s -> fillScope(s, principalId, issuerClientId))
                .collect(Collectors.toSet());
        Map<String, Set<JsonObject>> rules = prepareRules(filledScopes.toArray(new Scope[filledScopes.size()]));

        for (Map.Entry<String, Set<JsonObject>> entry : rules.entrySet()) {
            Set<JsonObject> audienceRules = entry.getValue();

            String keyForAuthorizationRules = authorizationRulesRepository.getKeyForAuthorizationRules(token, entry.getKey());
            if (authorizationRulesRepository.existsRules(keyForAuthorizationRules)) {
                authorizationRulesRepository
                        .addRules(keyForAuthorizationRules, audienceRules.toArray(new JsonObject[audienceRules.size()]));
            } else {
                // If is a new audience, we use the iam rules expire time because always iam has scopes for a token.
                String iamKey = authorizationRulesRepository.getKeyForAuthorizationRules(token, iamAudience);
                // Redis returns time to expire in seconds
                authorizationRulesRepository.save(keyForAuthorizationRules,
                        TimeUnit.SECONDS.toMillis(authorizationRulesRepository.getTimeToExpire(iamKey)),
                        audienceRules.toArray(new JsonObject[audienceRules.size()]));
            }
        }
    }

    @Cacheable(EXPAND_SCOPES_CACHE)
    @Override
    public Set<Scope> expandScopes(Collection<String> scopes) {
        if (CollectionUtils.isEmpty(scopes)) {
            return Collections.emptySet();
        }
        Validate.notNull(scopes);
        Validate.noNullElements(scopes);
        HashSet<Scope> expandedScopes = new HashSet<>();
        HashSet<String> processedCompositeScopes = new HashSet<>();
        List<Scope> scopesToProcess = new ArrayList<>(scopes.size());
        scopesToProcess.addAll(getScopes(scopes));
        while (!scopesToProcess.isEmpty()) {
            Scope scope = scopesToProcess.remove(0);
            if (scope.isComposed()) {
                if (processedCompositeScopes.add(scope.getId())) {
                    scopesToProcess.addAll(getScopes(scope.getScopes()));
                }
            } else {
                expandedScopes.add(scope);
            }
        }
        return expandedScopes;
    }

    @Override
    public Set<String> expandScopesIds(Set<String> scopes) {
        return expandScopes(scopes).stream().map(Scope::getIdWithParameters).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllowedScopes(AuthorizationRequestContext context) {
        Domain requestedDomain = context.getRequestedDomain();
        Set<String> domainScopes = expandScopesIds(requestedDomain.getScopes());

        if (context.isCrossDomain()) {
            return domainScopes;
        }

        Set<String> clientScopes = expandScopesIds(context.getIssuerClient().getScopes());
        Set<String> requestedScopes;

        if (context.hasPrincipal()) {
            Set<String> userScopes = expandScopesIds(context.getPrincipal().getScopes());
            requestedScopes = Sets.union(userScopes, clientScopes);
        } else {
            requestedScopes = clientScopes;
        }

        return Sets.intersection(requestedScopes, domainScopes);
    }

    @Override
    public void create(Scope scope) throws ScopeNameException {
        if (scope.getId().contains(";")) {
            throw new ScopeNameException();
        }

        scopeRepository.save(scope);
    }

    @Override
    public void delete(String scope) {
        scopeRepository.delete(scope);
    }

    private Map<String, Set<JsonObject>> prepareRules(Scope... scopes) {
        Validate.noNullElements(scopes);
        Map<String, Set<JsonObject>> rules = new HashMap<>();
        for (Scope scope : scopes) {
            Set<JsonObject> audienceRules = rules.get(scope.getAudience());
            if (audienceRules == null) {
                audienceRules = new HashSet<>();
                rules.put(scope.getAudience(), audienceRules);
            }
            audienceRules.addAll(scope.getRules());
        }
        return rules;
    }

    private long getMillisTo(long tokenExpirationTime) {
        return tokenExpirationTime - clock.millis();
    }

    private Map<String, String> createDefaultParams(String... keyAndValues) {
        Map<String, String> map = new HashMap<>(keyAndValues.length / 2);
        for (int i = 0; i < keyAndValues.length; i += 2) {
            map.put(keyAndValues[i], keyAndValues[i + 1]);
        }
        return map;
    }

}
