package com.bq.oss.corbel.iam.auth.rule;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Domain;

@RunWith(MockitoJUnitRunner.class) public class RequestDomainAuthorizationRuleTest {

    @Mock private AuthorizationRequestContext context;
    @Mock private Domain domainIssuer;
    @Mock private Domain domainRequested;

    private RequestDomainAuthorizationRule rule;

    @Before
    public void setup() {
        rule = new RequestDomainAuthorizationRule();
    }

    @Test
    public void processOk() throws UnauthorizedException {
        when(domainIssuer.getAllowedDomains()).thenReturn("exp.*");
        when(domainIssuer.getId()).thenReturn("test:level1");
        when(context.getIssuerClientDomain()).thenReturn(domainIssuer);
        when(context.getRequestedDomain()).thenReturn(domainRequested);

        when(domainRequested.getId()).thenReturn("test:level1");
        rule.process(context);

        when(domainRequested.getId()).thenReturn("test:level1:level2:level3");
        rule.process(context);

        when(domainRequested.getId()).thenReturn("expression");
        rule.process(context);

        when(context.getRequestedDomain()).thenReturn(domainIssuer);
        rule.process(context);
    }

    @Test(expected = UnauthorizedException.class)
    public void processFailNotAllowedDomain() throws UnauthorizedException {
        when(domainIssuer.getAllowedDomains()).thenReturn("exp.*");
        when(domainIssuer.getId()).thenReturn("test:level1");
        when(context.getIssuerClientDomain()).thenReturn(domainIssuer);
        when(context.getRequestedDomain()).thenReturn(domainRequested);

        when(domainRequested.getId()).thenReturn("epxression");
        rule.process(context);
    }

    @Test(expected = UnauthorizedException.class)
    public void processFailNotChildren() throws UnauthorizedException {
        when(domainIssuer.getId()).thenReturn("test:level1");
        when(context.getIssuerClientDomain()).thenReturn(domainIssuer);
        when(context.getRequestedDomain()).thenReturn(domainRequested);

        when(domainRequested.getId()).thenReturn("level1:level2:level3");
        rule.process(context);
    }

}
