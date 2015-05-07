package com.bq.oss.corbel.iam.auth.rule;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Client;

@RunWith(MockitoJUnitRunner.class) public class ClientSideAuthenticationAllowedAuthorizationRuleTest {

    private ClientSideAuthenticationAllowedAuthorizationRule rule;
    @Mock private AuthorizationRequestContext contextMock;
    @Mock private Client clientMock;

    @Before
    public void setup() {
        rule = new ClientSideAuthenticationAllowedAuthorizationRule();
    }

    @Test
    public void testOk() throws UnauthorizedException {
        when(clientMock.getClientSideAuthentication()).thenReturn(true);
        when(contextMock.getIssuerClient()).thenReturn(clientMock);
        when(contextMock.hasPrincipal()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test(expected = UnauthorizedException.class)
    public void testWithoutAttribute() throws UnauthorizedException {
        when(clientMock.getClientSideAuthentication()).thenReturn(null);
        when(contextMock.getIssuerClient()).thenReturn(clientMock);
        when(contextMock.hasPrincipal()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test(expected = UnauthorizedException.class)
    public void testWithFalseAttribute() throws UnauthorizedException {
        when(clientMock.getClientSideAuthentication()).thenReturn(false);
        when(contextMock.getIssuerClient()).thenReturn(clientMock);
        when(contextMock.hasPrincipal()).thenReturn(true);
        rule.process(contextMock);
    }

    @Test
    public void testWithFalseAttributeButNotPrn() throws UnauthorizedException {
        when(clientMock.getClientSideAuthentication()).thenReturn(false);
        when(contextMock.getIssuerClient()).thenReturn(clientMock);
        when(contextMock.hasPrincipal()).thenReturn(false);
        rule.process(contextMock);
    }
}
