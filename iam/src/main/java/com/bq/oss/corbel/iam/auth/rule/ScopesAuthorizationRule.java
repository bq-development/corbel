package com.bq.oss.corbel.iam.auth.rule;

import java.util.Set;
import java.util.stream.Collectors;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.auth.AuthorizationRule;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Scope;
import com.bq.oss.corbel.iam.service.ScopeService;
import com.bq.oss.corbel.iam.utils.Message;
import com.google.common.collect.Sets;

/**
 * @author Alberto J. Rubio
 */
public class ScopesAuthorizationRule implements AuthorizationRule {

    private final ScopeService scopeService;

    public ScopesAuthorizationRule(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {

        Set<Scope> requestedExpandScopes, domainScopes, clientScopes = null, userScopes = null, groupScopes = null;

        domainScopes = scopeService.expandScopes(context.getRequestedDomain().getScopes());

        if(!context.isCrossDomain()) {
            clientScopes = scopeService.expandScopes(context.getIssuerClient().getScopes());
            if(context.hasPrincipal()) {
                userScopes = scopeService.expandScopes(context.getPrincipal().getScopes());
                groupScopes = scopeService.expandScopes(scopeService.getGroupScopes(context.getPrincipal().getGroups()));
            }
        }

        Set<Scope> allowedScopes = scopeService.getAllowedScopes(domainScopes, clientScopes, userScopes, groupScopes,
                                    context.isCrossDomain(), context.hasPrincipal());


        if (!context.getRequestedScopes().isEmpty()) {
            requestedExpandScopes = scopeService.expandScopes(context.getRequestedScopes());
            checkRequestedScopes(requestedExpandScopes, allowedScopes);
        } else {
            requestedExpandScopes = allowedScopes;
        }
        context.setExpandedRequestedScopes(requestedExpandScopes);
    }

    private void checkRequestedScopes(Set<Scope> requestedExpandScopes, Set<Scope> allowedScopes) throws UnauthorizedException {
        if (!allowedScopes.containsAll(requestedExpandScopes)) {
            Set<String> requestedScopes = requestedExpandScopes.stream().map(Scope::getIdWithParameters).collect(Collectors.toSet());
            Set<String> allowedScopesIds = allowedScopes.stream().map(Scope::getIdWithParameters).collect(Collectors.toSet());
            throw new UnauthorizedException(Message.REQUESTED_SCOPES_UNAUTHORIZED.getMessage(Sets.difference(requestedScopes,
                    allowedScopesIds)));
        }
    }

}
