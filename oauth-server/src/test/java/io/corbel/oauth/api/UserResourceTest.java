package io.corbel.oauth.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.BasicAuthProvider;
import io.corbel.lib.ws.auth.OAuthProvider;
import io.corbel.oauth.TestUtils;
import io.corbel.oauth.api.auth.ClientCredentialsAuthenticator;
import io.corbel.oauth.api.auth.TokenAuthenticator;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.Role;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.ClientRepository;
import io.corbel.oauth.repository.CreateUserException;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.UserService;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Cristian del Cerro
 */
public class UserResourceTest extends UserResourceTestBase{

    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new UserResource(userServiceMock, clientServiceMock))
            .addProvider(new ContextInjectableProvider<>(HttpServletRequest.class, requestMock))
            .addProvider(new BasicAuthProvider(basicAuthFactory).getBinder()).addProvider(new OAuthProvider(oAuthFactory).getBinder())
            .build();

    @Test
    public void getUserGoodTokenTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        User userTestWithoutPassword = TestUtils.createUserTest(Role.USER);
        userTestWithoutPassword.setPassword(null);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(User.class)).isEqualTo(userTestWithoutPassword);

    }

    @Test
    public void getUserGoodTokenUsingUserIdTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        User userTestWithoutPassword = TestUtils.createUserTest(Role.USER);
        userTestWithoutPassword.setPassword(null);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(User.class)).isEqualTo(userTestWithoutPassword);

    }

    @Test
    public void getUserGoodTokenUsingDifferentUserIdTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        User userTestWithoutPassword = TestUtils.createUserTest(Role.USER);
        userTestWithoutPassword.setPassword(null);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request()
                .header("Authorization", BEARER + TEST_ADMIN_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(User.class)).isEqualTo(userTestWithoutPassword);

    }

    @Test
    public void getUserGoodTokenUsingWrongUserIdTest() throws TokenVerificationException {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + "bad_id").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getUserGoodTokenUsingRootUserTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.ROOT);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_ADMIN_TOKEN);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request()
                .header("Authorization", BEARER + TEST_ADMIN_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(404);

    }

    @Test
    public void getProfileGoodTokenTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        User userTestWithoutPassword = TestUtils.createUserTest(Role.USER);
        userTestWithoutPassword.setPassword(null);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID + "/profile").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(User.class)).isEqualTo(TestUtils.getProfileUserTest());
    }

    @Test
    public void deleteUserGoodTokenTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        when(userServiceMock.getUser(USERNAME_TEST)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void deleteUserGoodTokenUsingUserIdTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);
        when(userServiceMock.getUser(USERNAME_TEST)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void deleteUserGoodTokenUsingDifferentUserIdTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        when(userServiceMock.getUser(USERNAME_TEST)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request()
                .header("Authorization", BEARER + TEST_ADMIN_TOKEN).delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void deleteUserGoodTokenUsingDifferentRootUserTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.ROOT);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);
        when(userServiceMock.getUser(USERNAME_TEST)).thenReturn(userTest);

        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_ADMIN_TOKEN);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request()
                .header("Authorization", BEARER + TEST_ADMIN_TOKEN).delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getProfileBadTokenTest() throws TokenVerificationException {
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_BAD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_BAD_TOKEN).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void deleteUserBadTokenTest() throws TokenVerificationException {

        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_BAD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_BAD_TOKEN).delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void getProfileNotBearerTokenTest() throws TokenVerificationException {
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_GOOD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request().header("Authorization", TEST_GOOD_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void deleteUserNotBearerTokenTest() throws TokenVerificationException {
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_GOOD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request().header("Authorization", TEST_GOOD_TOKEN)
                .delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void createUser() throws CreateUserException.DuplicatedUser, TokenVerificationException {
        User user = createTestUser();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userServiceMock.createUser(userCaptor.capture(), Mockito.eq(TEST_CLIENT))).thenReturn(USERNAME_TEST);

        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_BASIC_CLIENT_CRED);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user").request()
                .header("Authorization", TEST_BASIC_CLIENT_CRED).post(Entity.json(user), Response.class);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(userCaptor.getValue().getId()).isNull();
        assertThat(userCaptor.getValue().getSalt()).isNotNull();
        assertThat(userCaptor.getValue().getPassword()).isNotNull();
        assertThat(userCaptor.getValue().getUsername()).isEqualTo(USERNAME_TEST);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    public void createUserWithProperties() throws CreateUserException.DuplicatedUser, TokenVerificationException {
        // SETUP
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.setUsername(USERNAME_TEST);

        Map<String, Object> properties = new HashMap<>();
        properties.put("propertieTestKey", "propertieTestValue");
        user.setProperties(properties);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userServiceMock.createUser(userCaptor.capture(), Mockito.eq(TEST_CLIENT))).thenReturn(USERNAME_TEST);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_BASIC_CLIENT_CRED);

        // LAUNCH
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user").request()
                .header("Authorization", TEST_BASIC_CLIENT_CRED).post(Entity.json(user), Response.class);

        // CHECK
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(userCaptor.getValue().getSalt()).isNotNull();
        assertThat(userCaptor.getValue().getPassword()).isNotNull();
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(userCaptor.getValue().getProperties()).isEqualTo(properties);
    }

    @Test
    public void createDuplicateUser() throws CreateUserException.DuplicatedUser {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.setUsername(USERNAME_TEST);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userServiceMock.createUser(userCaptor.capture(), Mockito.eq(TEST_CLIENT))).thenReturn(USERNAME_TEST);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_BASIC_CLIENT_CRED);

        doThrow(new CreateUserException.DuplicatedUser()).when(userServiceMock).createUser(userCaptor.capture(), Mockito.eq(TEST_CLIENT));
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user").request()
                .header("Authorization", TEST_BASIC_CLIENT_CRED).post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(409);
    }

    @Test
    public void createUserMissingClientCredential() throws CreateUserException.DuplicatedUser {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user").request()
                .post(Entity.json(TestUtils.createUserTest(Role.USER)), Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void updateProfileGoodTokenTest() throws TokenVerificationException {

        User userTest = TestUtils.createUserTest(Role.USER);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).put(Entity.json(userTest), Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void updateProfileGoodTokenUsingUserIdTest() throws TokenVerificationException {

        User userTest = TestUtils.createUserTest(Role.USER);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).put(Entity.json(userTest), Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void updateProfileGoodTokenUsingDifferentUserIdTest() throws TokenVerificationException {

        User userTest = TestUtils.createUserTest(Role.USER);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER + TEST_ADMIN_TOKEN).put(Entity.json(userTest), Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void updateProfileGoodTokenUsingDifferentUserAdminIdTest() throws TokenVerificationException {

        User userTest = TestUtils.createUserTest(Role.ADMIN);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_ADMIN_TOKEN);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID).request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER + TEST_ADMIN_TOKEN).put(Entity.json(userTest), Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void updateProfileBadTokenTest() throws TokenVerificationException {
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_BAD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request(MediaType.APPLICATION_JSON)
                .header("Authorization", BEARER + TEST_BAD_TOKEN).put(Entity.json(TestUtils.createUserTest(Role.USER)), Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void updateProfileNotBearerTokenTest() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_GOOD_TOKEN);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request(MediaType.APPLICATION_JSON)
                .header("Authorization", TEST_GOOD_TOKEN).put(Entity.json(userTest), Response.class);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void getUserValidateEmail() throws TokenVerificationException {
        // CONFIGURE
        User userTest = TestUtils.createUserTest(Role.USER);
        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(userTest);
        when(clientServiceMock.findByName(TEST_CLIENT_ID)).thenReturn(Optional.of(TEST_CLIENT));
        // LAUNCH
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me/validate").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void getUserValidateBadToken() throws TokenVerificationException {
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_BAD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me/validate").request()
                .header("Authorization", BEARER + TEST_BAD_TOKEN).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void putUserValidateEmail() throws TokenVerificationException {
        // CONFIGURE
        User user = new User();
        user.setEmailValidated(true);
        when(clientServiceMock.findByName(TEST_CLIENT_ID)).thenReturn(Optional.of(TEST_CLIENT));
        // LAUCNH
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).put(Entity.json(user), Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void putUserValidateEmailBadToken() throws TokenVerificationException {
        User user = new User();
        user.setEmailValidated(true);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_BAD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_BAD_TOKEN).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void getResetPasswordEmail() throws TokenVerificationException {
        User userTest = TestUtils.createUserTest(Role.USER);

        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_BASIC_CLIENT_CRED);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/resetPassword?email=" + userTest.getEmail()).request()
                .header("Authorization", TEST_BASIC_CLIENT_CRED).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void getResetPasswordEmailBadToken() throws TokenVerificationException {
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/resetPassword").request()
                .header("Authorization", TEST_BAD_TOKEN).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void postResetPasswordEmail() throws TokenVerificationException {
        User user = new User();
        user.setPassword("xxxx");
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void postResetPasswordEmailBadToken() throws TokenVerificationException {
        User user = new User();
        user.setPassword("xxxx");

        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_BAD_TOKEN);
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/me").request()
                .header("Authorization", BEARER + TEST_BAD_TOKEN).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void getAvatarTest() {
        User user = mock(User.class);
        user.setId(TEST_USER_ID);
        user.setAvatarUri(TEST_AVATAR_URI);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(user);
        when(user.getAvatarUri()).thenReturn(TEST_AVATAR_URI);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID + "/avatar")
                .property(ClientProperties.FOLLOW_REDIRECTS, false).request().header("Authorization", BEARER + TEST_GOOD_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.TEMPORARY_REDIRECT.getStatusCode());
        assertThat(java.util.Optional.ofNullable(response.getHeaders().get("Location"))
                .map(locations -> locations.contains(TEST_AVATAR_URI)).orElse(false)).isTrue();
    }

    @Test
    public void getInexistentAvatarTest() {
        User user = mock(User.class);
        user.setId(TEST_USER_ID);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(user);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/user/" + TEST_USER_ID + "/avatar").request()
                .header("Authorization", BEARER + TEST_GOOD_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}