package io.corbel.iam.auth.rule;

import static org.mockito.Mockito.when;

import io.corbel.iam.model.Domain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.exception.UnauthorizedException;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class) public class AuthenticationTypeAllowedAuthorizationRuleTest {

    private AuthenticationTypeAllowedAuthorizationRule rule;
    @Mock private AuthorizationRequestContext contextMock;
    @Mock private Domain domainMock;

    @Before
    public void setup() {
        rule = new AuthenticationTypeAllowedAuthorizationRule();
    }

    @Test
    public void testOkWithBasicAuthenticationWithDefaultCapabilities() throws UnauthorizedException {
        when(domainMock.getCapabilities()).thenReturn(Collections.emptyMap());
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);
        when(contextMock.isBasic()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test
    public void testOkWithBasicAuthentication() throws UnauthorizedException {
        when(domainMock.getCapabilities()).thenReturn(Collections.singletonMap("basic", true));
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);
        when(contextMock.isBasic()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test(expected = UnauthorizedException.class)
    public void testErrorWithBasicAuthenticationAndCapabilityDisabled() throws UnauthorizedException {
        when(domainMock.getCapabilities()).thenReturn(Collections.singletonMap("basic", false));
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);
        when(contextMock.isBasic()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test
    public void testOkWithOauthAuthenticationWithDefaultCapabilities() throws UnauthorizedException {
        when(domainMock.getCapabilities()).thenReturn(Collections.emptyMap());
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);
        when(contextMock.isOAuth()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test
    public void testOkWithOauthAuthentication() throws UnauthorizedException {
        when(domainMock.getCapabilities()).thenReturn(Collections.singletonMap("oauth", true));
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);
        when(contextMock.isOAuth()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test(expected = UnauthorizedException.class)
    public void testErrorWithOauthAuthenticationAndCapabilityDisabled() throws UnauthorizedException {
        when(domainMock.getCapabilities()).thenReturn(Collections.singletonMap("oauth", false));
        when(contextMock.getRequestedDomain()).thenReturn(domainMock);
        when(contextMock.isOAuth()).thenReturn(true);
        rule.process(contextMock);
    }
}
