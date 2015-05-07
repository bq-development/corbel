package com.bq.oss.corbel.iam.service;

import java.util.Collection;
import java.util.Set;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.exception.ScopeNameException;
import com.bq.oss.corbel.iam.model.Scope;

/**
 * Business logic for Scope management.
 * 
 * @author Alexander De Leon
 * 
 */
public interface ScopeService {

    Scope getScope(String id);

    Set<Scope> getScopes(Collection<String> scopes);

    Set<Scope> getScopes(String... scopes);

    Scope fillScope(Scope scope, String userId, String clientId);

    void addAuthorizationRules(String token, Set<String> scopes, String principalId, String issuerClientId);

    Set<Scope> expandScopes(Collection<String> scopes);

    Set<String> expandScopesIds(Set<String> requestedScopes);

    void publishAuthorizationRules(String token, long tokenExpirationTime, Set<String> scopes, String principalId, String issuerClientId);

    Set<String> getAllowedScopes(AuthorizationRequestContext authorizationRequestContext);

    void create(Scope scope) throws ScopeNameException;

    void delete(String scope);
}
