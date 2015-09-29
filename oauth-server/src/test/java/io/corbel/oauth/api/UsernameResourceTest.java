package io.corbel.oauth.api;

import com.google.common.base.Optional;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.*;
import io.corbel.oauth.model.User;
import io.corbel.oauth.service.UserService;
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
import static org.mockito.Mockito.*;

/**
 * @author Ricardo Martínez
 */
public class UsernameResourceTest extends UserResourceTestBase{

    private static final String URL_PREFIX = "/" + ApiVersion.CURRENT + "/username/";

    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new UsernameResource(userServiceMock))
            .addProvider(new ContextInjectableProvider<>(HttpServletRequest.class, requestMock))
            .addProvider(new BasicAuthProvider(basicAuthFactory).getBinder())
            .addProvider(new OAuthProvider(oAuthFactory).getBinder())
            .build();

    @Test
    public void testExistUser() {
        User user = createTestUser();
        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(user);
        when(userServiceMock.existsByUsernameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(true);
        Response response = RULE.client().target(URL_PREFIX + USERNAME_TEST).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .head();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNotExistUser() {
        when(userServiceMock.existsByUsernameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(false);
        Response response = RULE.client().target(URL_PREFIX + USERNAME_TEST).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .head();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetUserIdByUsernameOK() {
        User user = createTestUser();
        when(userServiceMock.findByUserNameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(user);
        User response = RULE.client().target(URL_PREFIX + USERNAME_TEST).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .get(User.class);
        verify(userServiceMock, times(1)).findByUserNameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID);

        assertEquals(response.getId(), TEST_USER_ID);
    }

    @Test
    public void testGetUserIdByUsernameKO() {
        User user = createTestUser();
        when(userServiceMock.findByUserNameAndDomain(USERNAME_TEST, TEST_DOMAIN_ID)).thenReturn(user);
        Response response = RULE.client().target(URL_PREFIX + USERNAME_TEST + Instant.now().toString()).request().header(AUTHORIZATION, "Bearer " + TEST_GOOD_TOKEN)
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
