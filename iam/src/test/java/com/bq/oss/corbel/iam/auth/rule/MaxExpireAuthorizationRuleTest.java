package com.bq.oss.corbel.iam.auth.rule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;

/**
 * @author Alexander De Leon
 * 
 */
public class MaxExpireAuthorizationRuleTest {

    private static final int TEST_MAX_EXPIRATION = 36000000;

    private MaxExpireAuthorizationRule rule;
    private AuthorizationRequestContext contextMock;

    @Before
    public void setup() {
        contextMock = mock(AuthorizationRequestContext.class);
        rule = new MaxExpireAuthorizationRule(TEST_MAX_EXPIRATION);
    }

    @Test
    public void testOk() throws UnauthorizedException {
        when(contextMock.getAuthorizationExpiration()).thenReturn(System.currentTimeMillis() + TEST_MAX_EXPIRATION);
        rule.process(contextMock);
    }

    @Test(expected = UnauthorizedException.class)
    public void testUnauthorized() throws UnauthorizedException {
        when(contextMock.getAuthorizationExpiration()).thenReturn(System.currentTimeMillis() + (TEST_MAX_EXPIRATION * 2));
        rule.process(contextMock);
    }

}
