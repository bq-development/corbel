package io.corbel.iam.auth.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.exception.NoSuchPrincipalException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Client;
import io.corbel.iam.model.User;
import io.corbel.iam.utils.Message;

public class PrincipalExistsAuthorizationRuleTest {

    private static final String TEST_PRINCIPAL_ID = "0";
    private static final String TEST_DOMAIN_ID = "test";
    private PrincipalExistsAuthorizationRule rule;
    private AuthorizationRequestContext context;

    @Before
    public void setUp() {
        context = Mockito.mock(AuthorizationRequestContext.class);
        rule = new PrincipalExistsAuthorizationRule();
    }

    @Test
    public void testNoPrincipalRequest() throws UnauthorizedException {
        when(context.hasPrincipal()).thenReturn(false);
        rule.process(context);
    }

    @Test
    public void testPrincipalExists() throws UnauthorizedException {
        when(context.hasPrincipal()).thenReturn(true);
        when(context.getPrincipal()).thenReturn(new User());
        rule.process(context);
    }

    @Test
    public void testPrincipalNotExistsException() throws UnauthorizedException {

        try {

            Client testClient = new Client();
            testClient.setDomain(TEST_DOMAIN_ID);

            when(context.hasPrincipal()).thenReturn(true);
            when(context.getPrincipalId()).thenReturn(TEST_PRINCIPAL_ID);
            when(context.getIssuerClient()).thenReturn(testClient);

            rule.process(context);
            fail();
        } catch (NoSuchPrincipalException e) {
            assertEquals(Message.PRINCIPAL_EXISTS_UNAUTHORIZED.getMessage(TEST_PRINCIPAL_ID, TEST_DOMAIN_ID), e.getMessage());
        }

    }
}
