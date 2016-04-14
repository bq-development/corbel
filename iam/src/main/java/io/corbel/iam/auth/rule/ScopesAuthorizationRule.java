package io.corbel.iam.auth.rule;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Scope;
import io.corbel.iam.service.GroupService;
import io.corbel.iam.service.ScopeService;
import io.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
public class ScopesAuthorizationRule implements AuthorizationRule {

    private final ScopeService scopeService;
    private final GroupService groupService;

    public ScopesAuthorizationRule(ScopeService scopeService, GroupService groupService) {
        this.scopeService = scopeService;
        this.groupService = groupService;
    }

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        Set<String> domainScopes = context.getRequestedDomain().getScopes();
        Set<String> requestedScopes = context.isCrossDomain() ? domainScopes : getRequestedScopes(context);
        Set<Scope> allowedScopes = getAllowedScopes(domainScopes, requestedScopes);
        if (context.getRequestedScopes().isEmpty()) {
            context.setExpandedRequestedScopes(allowedScopes);
            context.setTokenScopes(requestedScopes);
        } else {
            Set<Scope> tokenRequestedScopes = scopeService.expandScopes(context.getRequestedScopes());
            checkRequestedScopes(tokenRequestedScopes, allowedScopes);
            context.setExpandedRequestedScopes(tokenRequestedScopes);
            context.setTokenScopes(context.getRequestedScopes());
        }
    }

    private Set<String> getRequestedScopes(AuthorizationRequestContext context) {
        Set<String> requestedScopes = context.getIssuerClient().getScopes();
        if (context.hasPrincipal()) {
            Set<String> userScopes = context.getPrincipal().getScopes();
            Set<String> groupScopes = groupService.getGroupScopes(context.getPrincipal().getGroups());
            requestedScopes = Sets.union(requestedScopes, Sets.union(userScopes, groupScopes));
        }
        return requestedScopes;
    }

    private Set<Scope> getAllowedScopes(Set<String> domainScopes, Set<String> requestedScopes) {
        return Sets.intersection(scopeService.expandScopes(requestedScopes), scopeService.expandScopes(domainScopes));
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
