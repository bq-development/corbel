package com.bq.oss.corbel.iam.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;

import com.bq.oss.corbel.iam.exception.ScopeNameException;
import com.bq.oss.corbel.iam.model.Scope;

/**
 * Business logic for Scope management.
 * 
 * @author Alexander De Leon
 * 
 */
public interface ScopeService {

    String EXPAND_SCOPES_CACHE = "expandScopesCache";

    Scope getScope(String id);

    Set<String> getGroupScopes(Collection<String> groups);

    Set<Scope> getScopes(Collection<String> scopes);

    Set<Scope> getScopes(String... scopes);

    Set<Scope> fillScopes(Set<Scope> filledScopes, String userId, String clientId);

    Scope fillScope(Scope scope, String userId, String clientId);

    void addAuthorizationRules(String token, Set<Scope> filledScopes);

    @Cacheable(EXPAND_SCOPES_CACHE)
    Set<Scope> expandScopes(Collection<String> scopes);

    void publishAuthorizationRules(String token, long tokenExpirationTime, Set<Scope> filledScopes);

    Set<Scope> getAllowedScopes(Set<Scope> domainScopes, Set<Scope> clientScopes, Set<Scope> userScopes,
                                Set<Scope> groupScopes, boolean isCrossDomain, boolean hasPrincipal);

    void create(Scope scope) throws ScopeNameException;

    void delete(String scope);


}
