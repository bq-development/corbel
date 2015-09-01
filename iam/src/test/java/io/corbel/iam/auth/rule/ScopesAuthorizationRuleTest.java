package io.corbel.iam.auth.rule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Client;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Scope;
import io.corbel.iam.model.User;
import io.corbel.iam.service.GroupService;
import io.corbel.iam.service.ScopeService;
import io.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
@RunWith(MockitoJUnitRunner.class) public class ScopesAuthorizationRuleTest {
    @Mock ScopeService scopeServiceMock;
    @Mock GroupService groupServiceMock;
    private AuthorizationRequestContext context;
    private ScopesAuthorizationRule rule;

    @Before
    public void setUp() {
        context = mock(AuthorizationRequestContext.class);
        rule = new ScopesAuthorizationRule(scopeServiceMock, groupServiceMock);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScopesAllowed() throws Exception {
        Domain domain = mock(Domain.class);
        Set<String> domainScopesIds = mock(Set.class);
        Set<Scope> domainScopes = mock(Set.class);
        Client client = mock(Client.class);
        Set<String> clientScopesIds = mock(Set.class);
        Set<Scope> clientScopes = mock(Set.class);
        User user = mock(User.class);
        Set<String> userScopesIds = mock(Set.class);
        Set<Scope> userScopes = mock(Set.class);

        when(context.getRequestedDomain()).thenReturn(domain);
        when(context.getIssuerClient()).thenReturn(client);
        when(context.getPrincipal()).thenReturn(user);

        when(scopeServiceMock.expandScopes(domainScopesIds)).thenReturn(domainScopes);
        when(scopeServiceMock.expandScopes(clientScopesIds)).thenReturn(clientScopes);
        when(scopeServiceMock.expandScopes(userScopesIds)).thenReturn(userScopes);

        when(context.getRequestedScopes()).thenReturn(new HashSet<>(Collections.emptyList()));

        rule.process(context);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScopesNotAllowed() throws Exception {
        try {
            Domain domain = mock(Domain.class);
            when(context.getRequestedDomain()).thenReturn(domain);
            Client client = mock(Client.class);
            when(context.getIssuerClient()).thenReturn(client);
            User user = mock(User.class);
            when(context.getPrincipal()).thenReturn(user);

            when(context.getRequestedScopes()).thenReturn(new HashSet<>(Collections.singletonList("SCOPE_4")));

            Scope scope = mock(Scope.class);
            when(scope.getIdWithParameters()).thenReturn("SCOPE_A");

            Set requestedScope = new HashSet<>(Collections.singletonList(scope));
            when(scopeServiceMock.expandScopes(eq(context.getRequestedScopes()))).thenReturn(requestedScope);
            rule.process(context);
            throw new Exception();
        } catch (UnauthorizedException e) {
            assertThat(e.getMessage())
                    .isEqualTo(Message.REQUESTED_SCOPES_UNAUTHORIZED.getMessage(new HashSet<>(Collections.singletonList("SCOPE_A"))));
        }
    }

}
