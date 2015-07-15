package com.bq.oss.corbel.iam.api;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.reader.TokenReader;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.AuthorizationInfoProvider;
import com.bq.oss.lib.ws.auth.AuthorizationRequestFilter;
import com.bq.oss.lib.ws.auth.BearerTokenAuthenticator;
import com.google.common.base.Optional;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.Instant;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class EmailResourceTest extends UserResourceTestBase {

    private static final UserService userServiceMock = mock(UserService.class);
    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final TokenReader tokenReaderMock = mock(TokenReader.class);
    private static final TokenInfo tokenMock = mock(TokenInfo.class);

    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, ""));

    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new EmailResource(userServiceMock))
            .addProvider(filter).addProvider(new AuthorizationInfoProvider().getBinder()).build();

    public EmailResourceTest() throws Exception {
        when(tokenMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getTokenReader()).thenReturn(tokenReaderMock);
        when(tokenReaderMock.getInfo()).thenReturn(tokenMock);
        when(tokenMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);
        when(tokenMock.getUserId()).thenReturn(TEST_USER_ID);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkAccessRules(eq(authorizationInfoMock), any());
    }

    @Test
    public void testCheckEmailAvailabilityOK() {
        User user = createTestUser();
        when(userServiceMock.findByDomainAndEmail(TEST_DOMAIN_ID, TEST_USER_EMAIL)).thenReturn(user);
        User response = RULE.client().target("/v1.0/email/" + TEST_USER_EMAIL).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(User.class);
        verify(userServiceMock, times(1)).findByDomainAndEmail(TEST_DOMAIN_ID, TEST_USER_EMAIL);

        assertEquals(response.getId(), TEST_USER_ID);
    }

    @Test
    public void testCheckEmailAvailabilityKO() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findByDomainAndEmail(TEST_DOMAIN_ID, TEST_USER_EMAIL)).thenReturn(user);
        Response response = RULE.client().target("/v1.0/email/" + TEST_USER_EMAIL + Instant.now().toString()).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
