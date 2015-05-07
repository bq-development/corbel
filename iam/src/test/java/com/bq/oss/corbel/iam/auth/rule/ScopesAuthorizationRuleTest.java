package com.bq.oss.corbel.iam.auth.rule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.service.ScopeService;
import com.bq.oss.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
@RunWith(MockitoJUnitRunner.class) public class ScopesAuthorizationRuleTest {
    @Mock ScopeService scopeServiceMock;
    private AuthorizationRequestContext context;
    private ScopesAuthorizationRule rule;

    @Before
    public void setUp() {
        context = Mockito.mock(AuthorizationRequestContext.class);
        rule = new ScopesAuthorizationRule(scopeServiceMock);
    }

    @Test
    public void testScopesAllowed() throws Exception {
        when(context.getRequestedScopes()).thenReturn(new HashSet<>(Arrays.asList()));
        when(scopeServiceMock.getAllowedScopes(eq(context))).thenReturn(new HashSet<>(Arrays.asList("SCOPE_3", "SCOPE_A")));
        when(scopeServiceMock.expandScopesIds(eq(context.getRequestedScopes()))).thenReturn(new HashSet<>(Arrays.asList("SCOPE_A")));
        rule.process(context);
    }

    @Test
    public void testScopesNotAllowed() throws Exception {
        try {
            when(context.getRequestedScopes()).thenReturn(new HashSet<>(Arrays.asList("SCOPE_4")));
            when(scopeServiceMock.getAllowedScopes(eq(context))).thenReturn(new HashSet<>(Arrays.asList("SCOPE_3")));
            when(scopeServiceMock.expandScopesIds(eq(context.getRequestedScopes()))).thenReturn(new HashSet<>(Arrays.asList("SCOPE_A")));
            rule.process(context);
            throw new Exception();
        } catch (UnauthorizedException e) {
            assertThat(e.getMessage()).isEqualTo(Message.REQUESTED_SCOPES_UNAUTHORIZED.getMessage(new HashSet<>(Arrays.asList("SCOPE_A"))));
        }
    }

}
