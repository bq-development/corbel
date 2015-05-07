package com.bq.oss.corbel.iam.auth.rule;

import java.util.Set;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.auth.AuthorizationRule;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
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
        Set<String> requestedScopes = context.getRequestedScopes();
        if (!requestedScopes.isEmpty()) {
            requestedScopes = scopeService.expandScopesIds(requestedScopes);
            Set<String> allowedScopes = scopeService.getAllowedScopes(context);
            if (!allowedScopes.containsAll(requestedScopes)) {
                throw new UnauthorizedException(Message.REQUESTED_SCOPES_UNAUTHORIZED.getMessage(Sets.difference(requestedScopes,
                        allowedScopes)));
            }
        }
    }

}
