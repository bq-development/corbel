package io.corbel.iam.auth.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.exception.InvalidVersionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Client;
import io.corbel.iam.utils.Message;

/**
 * @author Alberto J. Rubio
 */
public class VersionAuthorizationRuleTest {

    private AuthorizationRequestContext context;
    private VersionAuthorizationRule rule;

    private String versionsSupported = ">=1.0.0 & <=1.1.0";

    @Before
    public void setUp() {
        context = Mockito.mock(AuthorizationRequestContext.class);
        rule = new VersionAuthorizationRule();
        Client testClient = new Client();
        testClient.setId("testClient");
        testClient.setVersion(versionsSupported);
        when(context.getIssuerClient()).thenReturn(testClient);
    }

    @Test
    public void testHasNotVersion() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(false);
        rule.process(context);
    }

    @Test
    public void testCorrectVersion() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(true);
        when(context.getVersion()).thenReturn("1.0.8");
        rule.process(context);
    }

    @Test
    public void testLimitVersion() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(true);
        when(context.getVersion()).thenReturn("1.0.0");
        rule.process(context);
    }

    @Test
    public void testBadVersion() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(true);
        when(context.getVersion()).thenReturn("1.4.6");
        try {
            rule.process(context);
        } catch (InvalidVersionException e) {
            assertEquals(Message.INVALID_VERSION.getMessage("1.4.6", versionsSupported), e.getMessage());
        }
    }

    @Test
    public void testBadLimitVersion() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(true);
        when(context.getVersion()).thenReturn("1.1.0");
        try {
            rule.process(context);
        } catch (InvalidVersionException e) {
            assertEquals(Message.INVALID_VERSION.getMessage("1.1.0", versionsSupported), e.getMessage());
        }
    }

    @Test
    public void testVersionWithLetters() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(true);
        when(context.getVersion()).thenReturn("malformed_version");
        try {
            rule.process(context);
        } catch (InvalidVersionException e) {
            assertEquals(Message.INVALID_VERSION.getMessage("malformed_version", versionsSupported), e.getMessage());
        }
    }

    @Test
    public void testVersionWithCommas() throws UnauthorizedException {
        when(context.hasVersion()).thenReturn(true);
        when(context.getVersion()).thenReturn("1,1,0");
        try {
            rule.process(context);
        } catch (InvalidVersionException e) {
            assertEquals(Message.INVALID_VERSION.getMessage("1,1,0", versionsSupported), e.getMessage());
        }
    }
}
