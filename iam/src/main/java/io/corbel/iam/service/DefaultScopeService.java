package io.corbel.iam.service;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.corbel.iam.exception.ScopeNameException;
import io.corbel.iam.model.Entity;
import io.corbel.iam.model.Scope;
import io.corbel.iam.repository.ScopeRepository;
import io.corbel.iam.scope.ScopeFillStrategy;
import io.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultScopeService implements ScopeService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultScopeService.class);
    private static final int SCOPE_ID_POSITION = 0;
    private static final int FIRST_PARAM_POSITION = 1;

    private final ScopeRepository scopeRepository;
    private final GroupService groupService;
    private final AuthorizationRulesRepository authorizationRulesRepository;
    private final ScopeFillStrategy fillStrategy;
    private final String iamAudience;
    private final Clock clock;

    private final EventsService eventsService;

    public DefaultScopeService(ScopeRepository scopeRepository, GroupService groupService,
            AuthorizationRulesRepository authorizationRulesRepository, ScopeFillStrategy fillStrategy, String iamAudience, Clock clock,
            EventsService eventsService) {
        this.scopeRepository = scopeRepository;
        this.groupService = groupService;
        this.authorizationRulesRepository = authorizationRulesRepository;
        this.fillStrategy = fillStrategy;
        this.iamAudience = iamAudience;
        this.clock = clock;
        this.eventsService = eventsService;
    }

    public DefaultScopeService(ScopeRepository scopeRepository, GroupService groupService,
            AuthorizationRulesRepository authorizationRulesRepository, ScopeFillStrategy fillStrategy, String iamAudience,
            EventsService eventsService) {
        this(scopeRepository, groupService, authorizationRulesRepository, fillStrategy, iamAudience, Clock.systemDefaultZone(),
                eventsService);
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

    @Override
    public Set<String> getGroupScopes(Collection<String> groups) {
        Set<String> scopes = new HashSet<>();
        groups.stream().forEach(groupId ->
            groupService.get(groupId).ifPresent(group -> scopes.addAll(group.getScopes()))
        );
        return scopes;
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
    public Set<Scope> fillScopes(Set<Scope> scope, String userId, String clientId, String domainId) {
        return scope.stream().map(s -> fillScope(s, userId, clientId, domainId)).collect(Collectors.toSet());
    }

    @Override
    public Scope fillScope(Scope scope, String userId, String clientId, String domainId) {
        Validate.notNull(scope, "scope must not be null");
        Validate.notNull(clientId, "clientId must not be null");
        Validate.notNull(domainId, "domainId must not be null");
        Map<String, String> params = createDefaultParams("userId", userId, "clientId", clientId, "domainId", domainId);
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
    public void publishAuthorizationRules(String token, long tokenExpirationTime, Set<Scope> filledScopes) {
        Validate.notNull(filledScopes);
        Validate.noNullElements(filledScopes);
        Map<String, Set<JsonObject>> rules = prepareRules(filledScopes.toArray(new Scope[filledScopes.size()]));

        for (Map.Entry<String, Set<JsonObject>> entry : rules.entrySet()) {
            Set<JsonObject> audienceRules = entry.getValue();

            String keyForAuthorizationRules = authorizationRulesRepository.getKeyForAuthorizationRules(token, entry.getKey());
            authorizationRulesRepository.save(keyForAuthorizationRules, getMillisTo(tokenExpirationTime),
                    audienceRules.toArray(new JsonObject[audienceRules.size()]));
        }
    }

    @Override
    public void addAuthorizationRules(String token, Set<Scope> filledScopes) {
        Map<String, Set<JsonObject>> rules = prepareRules(filledScopes.toArray(new Scope[filledScopes.size()]));
        for (Map.Entry<String, Set<JsonObject>> entry : rules.entrySet()) {
            Set<JsonObject> audienceRules = entry.getValue();

            String keyForAuthorizationRules = authorizationRulesRepository.getKeyForAuthorizationRules(token, entry.getKey());
            if (authorizationRulesRepository.existsRules(keyForAuthorizationRules)) {
                authorizationRulesRepository.addRules(keyForAuthorizationRules,
                        audienceRules.toArray(new JsonObject[audienceRules.size()]));
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
    public Set<Scope> getAllowedScopes(Set<Scope> domainScopes, Set<Scope> clientScopes, Set<Scope> userScopes, Set<Scope> groupScopes,
            boolean isCrossDomain, boolean hasPrincipal) {

        if (isCrossDomain) {
            return domainScopes;
        }

        Set<Scope> requestedScopes;

        if (hasPrincipal) {
            requestedScopes = Sets.union(clientScopes, Sets.union(userScopes, groupScopes));
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
        eventsService.sendCreateScope(scope.getIdWithParameters());
    }

    @Override
    public void delete(String scope) {
        scopeRepository.delete(scope);
        eventsService.sendDeleteScope(scope);
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
