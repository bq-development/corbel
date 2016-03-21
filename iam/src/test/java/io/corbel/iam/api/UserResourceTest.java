package io.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Clock;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import io.corbel.lib.ws.gson.GsonMessageReaderWriterProvider;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import io.corbel.iam.exception.IdentityAlreadyExistsException;
import io.corbel.iam.exception.UserProfileConfigurationException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.model.User;
import io.corbel.iam.model.UserWithIdentity;
import io.corbel.iam.repository.CreateUserException;
import io.corbel.iam.service.DeviceService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.IdentityService;
import io.corbel.iam.service.UserService;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.exception.MalformedJsonQueryException;
import io.corbel.lib.queries.parser.*;
import io.corbel.lib.queries.request.*;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.api.error.JsonValidationExceptionMapper;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.auth.AuthorizationInfoProvider;
import io.corbel.lib.ws.auth.AuthorizationRequestFilter;
import io.corbel.lib.ws.model.Error;
import io.corbel.lib.ws.queries.QueryParametersProvider;

import com.google.common.collect.Sets;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Alexander De Leon
 */
public class UserResourceTest extends UserResourceTestBase {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;
    private static final String TEST_AVATAR_URI = "http://jklsdfjklasdfjkl.com/jsdklfjasdkl.png";

    private static final SortParser sortParserMock = mock(SortParser.class);
    private static final SearchParser searchParserMock = mock(SearchParser.class);
    private static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    private static final PaginationParser paginationParserMock = mock(PaginationParser.class);
    private static final UserService userServiceMock = mock(UserService.class);
    private static final DomainService domainServiceMock = mock(DomainService.class);
    private static final IdentityService identityServiceMock = mock(IdentityService.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final QueryParser queryParserMock = mock(QueryParser.class);
    private static final DeviceService devicesServiceMock = mock(DeviceService.class);
    public static final String NOT_ALLOWED_SCOPE = "notAllowedScope";
    private static AggregationResultsFactory<JsonElement> aggregationResultsFactory = new JsonAggregationResultsFactory();

    @SuppressWarnings("unchecked")
    private static final Authenticator<String, AuthorizationInfo> authenticator = mock(Authenticator.class);

    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticator, "realm", AuthorizationInfo.class);

    @SuppressWarnings("unchecked") private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, "", false, "user"));

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addProvider(new GsonMessageReaderWriterProvider())
            .addResource(new UserResource(userServiceMock, domainServiceMock, identityServiceMock, devicesServiceMock, aggregationResultsFactory, Clock.systemUTC()))
            .addProvider(filter)
            .addProvider(new AuthorizationInfoProvider().getBinder())
            .addProvider(
                    new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, new QueryParametersParser(queryParserMock,
                            aggregationParserMock, sortParserMock, paginationParserMock, searchParserMock)).getBinder())
            .addProvider(GenericExceptionMapper.class).addProvider(JsonValidationExceptionMapper.class).build();

    public UserResourceTest() throws Exception {
        when(authorizationInfoMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(authenticator.authenticate(any())).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
    }

    @Override
    protected ResourceTestRule getTestRule() {
        return RULE;
    }

    @Before
    public void setUp() {
        reset(userServiceMock, domainServiceMock, identityServiceMock);
        when(TEST_DOMAIN.getDefaultScopes()).thenReturn(ImmutableSet.of("defaultScope1", "defaultScope2"));
        when(domainServiceMock.getDomain(TEST_DOMAIN_ID)).thenReturn(Optional.of(TEST_DOMAIN));
    }

    @Test
    public void testAddUser() throws CreateUserException {
        ArgumentCaptor<User> userCaptor = forClass(User.class);

        User user = getTestUser();
        user.setScopes(ImmutableSet.of(NOT_ALLOWED_SCOPE));

        User userWithCreationDate = getTestUser();
        userWithCreationDate.setCreatedDate(new Date());
        when(userServiceMock.create(Mockito.any(User.class))).thenReturn(userWithCreationDate);

        Response response = addUserClient().post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEST_USER_ID);
        verify(userServiceMock).create(userCaptor.capture());
        User storeUser = userCaptor.getValue();
        assertThat(storeUser.getScopes()).isEqualTo(TEST_DOMAIN.getDefaultScopes());
        assertThat(storeUser.getScopes()).doesNotContain(NOT_ALLOWED_SCOPE);
    }

    @Test
    public void testAddUserWithNullScopes() throws CreateUserException {
        ArgumentCaptor<User> userCaptor = forClass(User.class);

        User user = getTestUser();
        user.setScopes(null);
        when(userServiceMock.create(Mockito.any(User.class))).thenReturn(user);

        Response response = addUserClient().post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEST_USER_ID);
        verify(userServiceMock).create(userCaptor.capture());
        User storeUser = userCaptor.getValue();
        assertThat(storeUser.getScopes()).isEqualTo(TEST_DOMAIN.getDefaultScopes());
    }

    @Test
    public void testAddUserWithBadIdentity() throws IdentityAlreadyExistsException, DuplicatedOauthServiceIdentityException,
            CreateUserException {
        when(domainServiceMock.oAuthServiceAllowedInDomain(any(), any())).thenReturn(true);
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        UserWithIdentity user = getTestUserWithIdentity();
        user.getIdentity().setOauthId("");
        user.getIdentity().setOauthService("");
        User userWithCreationDate = getTestUser();

        when(identityServiceMock.addIdentity(user.getIdentity())).thenThrow(new IllegalArgumentException());

        userWithCreationDate.setCreatedDate(new Date());

        when(userServiceMock.create(Mockito.any(User.class))).thenReturn(userWithCreationDate);
        Response response = addUserClient().post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(Error.class).getError()).isEqualTo("invalid_argument");
    }

    @SuppressWarnings("unused")
    @Test
    public void testUserAlreadyExists() throws CreateUserException {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        User user = getTestUser();
        user.setId(null);
        when(userServiceMock.create(any())).thenThrow((Class) CreateUserException.class);

        Response response = addUserClient().post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.readEntity(Error.class).getError()).isEqualTo("entity_exists");
    }

    @Test
    public void testCreateUserMissingUsername() {
        User user = createTestUser();
        user.setUsername(null);
        Response response = addUserClient().post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testCreateuserMissingEmail() {
        User user = createTestUser();
        user.setEmail(null);
        Response response = addUserClient().post(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testScopeNotAllowed() throws CreateUserException {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(false);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(userServiceMock.create(Mockito.eq(removeId(createTestUser())))).thenReturn(user);
        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    public void testGetUser() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        Response response = getUserClient(TEST_USER_ID).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(User.class)).isEqualsToByComparingFields(user);
    }

    @Test
    public void testGetUserAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";
        Aggregation operation = new Count("*");
        ResourceQuery targetQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(targetQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        JsonElement expectedResult = aggregationResultsFactory.countResult(4);
        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        when(userServiceMock.countUsersByDomain(eq(TEST_DOMAIN_ID), eq(targetQuery))).thenReturn(4L);

        Response response = getTestRule().client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo(expectedResult.toString());
    }

    @Test
    public void testGetUserInvalidAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";

        class OtherAggregation implements Aggregation {

            @Override
            public List<ResourceQuery> operate(List<ResourceQuery> list) {
                return null;
            }

            @Override
            public AggregationOperator getOperator() {
                return null;
            }
        }

        Aggregation operation = new OtherAggregation();
        ResourceQuery targetQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(targetQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        Response response = getTestRule().client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetUserMe() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        Response response = getUserClientMe().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(User.class)).isEqualsToByComparingFields(user);
    }

    @Test
    public void testGetUserMeOutOfDomain() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn("other_domain");
        Response response = getUserClientMe().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteUser() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        Response response = getUserClient(TEST_USER_ID).delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
        verify(userServiceMock).delete(user);
        verify(identityServiceMock).deleteUserIdentities(user);
        verify(devicesServiceMock).deleteByUserId(user);
    }

    @Test
    public void testDeleteMissingUser() {
        when(userServiceMock.findUserDomain("fakeId")).thenReturn(null);
        Response response = getUserClient("fakeId").delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
        verifyNoMoreInteractions(identityServiceMock, devicesServiceMock);
        verify(userServiceMock, never()).delete(any());
    }

    @Test
    public void testGetUserOutOfDomain() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_OTHER_DOMAIN);
        Response response = getUserClient(TEST_USER_ID).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteUserOutOfDomain() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(new User());
        Response response = getUserClient(TEST_USER_ID).delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
        verify(userServiceMock, never()).delete(any());
    }

    @Test
    public void testUpdateNotInDomain() {
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_OTHER_DOMAIN);
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        String newEmail = "new_email@test";
        User user = new User();
        user.setEmail(newEmail);

        Response response = getUserClientInOtherEmail(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testUpdateEmail() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "asdf@asdf.com";
        User user = new User();
        user.setEmail(newEmail);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(newEmail);
    }

    @Test
    public void testUpdateInvalidEmail() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "invalidEmail";
        User user = new User();
        user.setEmail(newEmail);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testUpdateEmptyEmail() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        User user = new User();
        user.setEmail("");

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testUpdateUsername() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newUsername = "newUsername";
        User user = new User();
        user.setUsername(newUsername);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getUsername()).isEqualTo(newUsername);
    }

    @Test
    public void testUpdateEmptyUsername() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        User user = new User();
        user.setEmail("");

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    /**
     * Coming 'EmailAndUsername' tests are importants cause email & username have special validation process.
     */
    @Test
    public void testUpdateEmailAndUsername() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "asdf@asdf.com";
        String newUsername = "newUsername";
        User user = new User();
        user.setEmail(newEmail);
        user.setUsername(newUsername);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getUsername()).isEqualTo(newUsername);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(newEmail);
    }

    @Test
    public void testUpdateEmailAndUsername2() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "";
        String newUsername = "newUsername";
        User user = new User();
        user.setEmail(newEmail);
        user.setUsername(newUsername);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testUpdateEmailAndUsername3() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "asdf@asdf.com";
        String newUsername = "";
        User user = new User();
        user.setEmail(newEmail);
        user.setUsername(newUsername);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testUpdateEmailAndUsername4() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "";
        String newUsername = "";
        User user = new User();
        user.setEmail(newEmail);
        user.setUsername(newUsername);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testUpdateName() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newFirstName = "my_f_name";
        String newLastName = "my_l_name";
        User user = new User();
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getFirstName()).isEqualTo(newFirstName);
        assertThat(userCaptor.getValue().getLastName()).isEqualTo(newLastName);
    }

    @Test
    public void testUpdatePhone() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newPhone = "my_number";
        User user = new User();
        user.setPhoneNumber(newPhone);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getPhoneNumber()).isEqualTo(newPhone);
    }

    @Test
    public void testUpdateScopes() {
        Set<String> newScopes = new HashSet<>(TEST_SCOPES);
        newScopes.add("new_scope");

        when(domainServiceMock.scopesAllowedInDomain(newScopes, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        User user = new User();
        user.setScopes(newScopes);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getScopes()).isEqualTo(newScopes);
    }

    @Test
    public void testUpdateScopesNotAllow() {
        Set<String> newScopes = new HashSet<>(TEST_SCOPES);
        newScopes.add("new_scope");

        when(domainServiceMock.scopesAllowedInDomain(newScopes, TEST_DOMAIN)).thenReturn(false);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        User user = new User();
        user.setScopes(newScopes);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    public void testSignOutMe() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/signout")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(""), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).signOut(TEST_USER_ID, java.util.Optional.of(TEST_TOKEN));
    }

    @Test
    public void testSignOutMeWithoutUser() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        when(authorizationInfoMock.getUserId()).thenReturn(null);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/signout")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(""), Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteSessionsMe() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/sessions")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).invalidateAllTokens(TEST_USER_ID);
    }

    @Test
    public void testDeleteSessionsMeWithoutUser() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        when(authorizationInfoMock.getUserId()).thenReturn(null);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/sessions")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);

    }

    @Test
    public void testGetSession(){
        when(userServiceMock.getSession(TEST_TOKEN)).thenReturn(getTestUserToken());
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/session")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(200);

        verify(userServiceMock).getSession(TEST_TOKEN);
    }

    @Test
    public void testGetSessionWithoutToken(){
        when(userServiceMock.getSession(TEST_TOKEN)).thenReturn(getTestUserToken());
        when(authorizationInfoMock.getToken()).thenReturn(null);
        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/session")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDisconnectMe() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/disconnect")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(""), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).signOut(TEST_USER_ID);
    }

    @Test
    public void testDisconnect() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/disconnect")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(""), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).signOut(TEST_USER_ID);
    }

    @Test
    public void testDisconnectNotFound() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/disconnect")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(""), Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testAddProperty() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        User user = new User();
        user.addProperty("a", "1");

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getProperties().get(TEST_PROPERTY)).isEqualTo(TEST_PROPERTY_VAL);
        assertThat(userCaptor.getValue().getProperties().get("a")).isEqualTo("1");
    }

    @Test
    public void testRemoveProperty() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        User user = new User();
        user.addProperty(TEST_PROPERTY, null);

        Response response = getUserClient(TEST_USER_ID).put(Entity.json(user), Response.class);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getProperties().isEmpty()).isTrue();
    }

    @Test
    public void addIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(201);

        ArgumentCaptor<Identity> captor = forClass(Identity.class);
        verify(identityServiceMock).addIdentity(captor.capture());

        assertThat(captor.getValue().getOauthId()).isEqualTo(oAuthId);
        assertThat(captor.getValue().getOauthService()).isEqualTo(oAuthService);
        assertThat(captor.getValue().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(captor.getValue().getDomain()).isEqualTo(TEST_DOMAIN_ID);
    }

    @Test
    public void addMeIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(201);

        ArgumentCaptor<Identity> captor = forClass(Identity.class);
        verify(identityServiceMock).addIdentity(captor.capture());

        assertThat(captor.getValue().getOauthId()).isEqualTo(oAuthId);
        assertThat(captor.getValue().getOauthService()).isEqualTo(oAuthService);
        assertThat(captor.getValue().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(captor.getValue().getDomain()).isEqualTo(TEST_DOMAIN_ID);
    }

    @Test
    public void addIdentityWithNullUser() {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addMeIdentityWithNullUser() {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addIdentityWithUserInOtherDomain() {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addIdentityMeWithUserInOtherDomain() {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void addDuplicatedIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        doThrow(new IdentityAlreadyExistsException()).when(identityServiceMock).addIdentity(identity);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void addDuplicatedOauthServiceIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        doThrow(new DuplicatedOauthServiceIdentityException()).when(identityServiceMock).addIdentity(identity);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void addDuplicatedMeIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        doThrow(new IdentityAlreadyExistsException()).when(identityServiceMock).addIdentity(identity);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void addIdentityWithoutOAuthServiceAllowedInDomain() {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(false);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void addIdentityMeWithoutOAuthServiceAllowedInDomain() {
        Identity identity = new Identity();
        String oAuthId = "socialId";
        identity.setOauthId(oAuthId);
        String oAuthService = "testService";
        identity.setOauthService(oAuthService);

        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(false);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(identity), Response.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void addGroupToUser() {
        User userWithGroup = createTestUser();
        Set<String> groups = new HashSet<>();
        groups.add("groupId");
        userWithGroup.addGroups(groups);

        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/"+TEST_USER_ID+"/groups")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(groups), Response.class);

        verify(userServiceMock).update(userWithGroup);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void deleteGroupToUser() {
        User userWithGroup = createTestUser();
        Set<String> groups = new HashSet<>();
        groups.add("groupId");
        userWithGroup.addGroups(groups);

        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(userWithGroup);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/"+TEST_USER_ID+"/groups/groupId")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(Response.class);

        User userWithGroupsEmpty = createTestUser();
        userWithGroupsEmpty.setGroups(new HashSet<>());

        verify(userServiceMock).update(userWithGroupsEmpty);
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testGetIdentities() {
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        Identity identity1 = new Identity();
        identity1.setId("TEST_ID");
        Identity identity2 = new Identity();
        identity2.setDomain("DOMAIN_ID");

        ArrayList<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);

        String oAuthService = "testService";
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);

        when(identityServiceMock.findUserIdentities(user)).thenReturn(identities);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/identity")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(new GenericType<List<Identity>>() {})).isEqualTo(identities);
    }

    @Test
    public void testMeGetIdentities() {
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        Identity identity1 = new Identity();
        identity1.setId("TEST_ID");
        Identity identity2 = new Identity();
        identity2.setDomain("DOMAIN_ID");

        ArrayList<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);

        String oAuthService = "testService";
        when(domainServiceMock.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).thenReturn(true);

        when(identityServiceMock.findUserIdentities(user)).thenReturn(identities);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(new GenericType<List<Identity>>() {})).isEqualTo(identities);
    }

    @Test
    public void testMeGetIdentitiesWithoutUser() {
        when(authorizationInfoMock.getUserId()).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/me/identity")
                .request(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetUserProfile() throws UserProfileConfigurationException {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);

        User profile = new User();
        profile.setEmail(TEST_USER_EMAIL);
        profile.setFirstName(TEST_USER_FIRST_NAME);
        when(userServiceMock.getUserProfile(user, TEST_DOMAIN.getUserProfileFields())).thenReturn(profile);

        Response response = getUserProfile(TEST_USER_ID).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(200);

        User userResponse = response.readEntity(User.class);
        assertThat(userResponse).isNotEqualTo(user);
        assertThat(userResponse.getEmail()).isEqualTo(user.getEmail());
        assertThat(userResponse.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userResponse.getDomain()).isNull();
    }

    @Test
    public void testGetUserProfileAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";
        Aggregation operation = new Count("*");
        ResourceQuery targetQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(targetQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        JsonElement expectedResult = aggregationResultsFactory.countResult(4L);
        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        when(userServiceMock.countUsersByDomain(eq(TEST_DOMAIN_ID), eq(targetQuery))).thenReturn(4L);

        Response response = getTestRule().client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/profile")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo(expectedResult.toString());
    }

    @Test
    public void testGetUserProfileInvalidAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";

        class OtherAggretation implements Aggregation {

            @Override
            public List<ResourceQuery> operate(List<ResourceQuery> list) {
                return null;
            }

            @Override
            public AggregationOperator getOperator() {
                return null;
            }
        }

        Aggregation operation = new OtherAggretation();
        ResourceQuery targetQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(targetQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        Response response = getTestRule().client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/profile")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetNotFoundUserProfile() throws UserProfileConfigurationException {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);

        when(userServiceMock.getUserProfile(user, TEST_DOMAIN.getUserProfileFields())).thenReturn(null);

        Response response = getUserProfile(TEST_USER_ID).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserProfileWithException() throws UserProfileConfigurationException {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);

        when(userServiceMock.getUserProfile(user, TEST_DOMAIN.getUserProfileFields()))
                .thenThrow(UserProfileConfigurationException.class);

        Response response = getUserProfile(TEST_USER_ID).get(Response.class);
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void testGetProfilesWithInvalidQuery() throws MalformedJsonQueryException, UnsupportedEncodingException,
            UserProfileConfigurationException {
        TEST_DOMAIN.setUserProfileFields(Sets.newHashSet("field1"));

        String queryString = "queryString";
        ResourceQuery targetQuery = new ResourceQueryBuilder().add("field1", "value1").add("field2", "value2").build();

        when(queryParserMock.parse(queryString)).thenReturn(targetQuery);
        Response response = getTestRule().client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/profile")
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        ArgumentCaptor<ResourceQuery> argument = forClass(ResourceQuery.class);
        verify(userServiceMock).findUserProfilesByDomain(eq(UserResourceTestBase.TEST_DOMAIN), argument.capture(), any(), any());

        assertThat(argument.getValue().iterator().next().getField()).isEqualTo("_notExistent");
    }

    @Test
    public void testGetProfilesWithValidQuery() throws MalformedJsonQueryException, UnsupportedEncodingException,
            UserProfileConfigurationException {
        when(TEST_DOMAIN.getUserProfileFields()).thenReturn(Sets.newHashSet("field1"));

        String queryString = "queryString";
        ResourceQuery targetQuery = new ResourceQueryBuilder().add("field1", "value1").build();

        when(queryParserMock.parse(queryString)).thenReturn(targetQuery);
        Response response = getTestRule().client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/profile")
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        ArgumentCaptor<ResourceQuery> argument = forClass(ResourceQuery.class);
        verify(userServiceMock).findUserProfilesByDomain(eq(UserResourceTestBase.TEST_DOMAIN), argument.capture(), any(), any());

        assertThat(argument.getValue().iterator().next().getField()).isEqualTo("field1");
    }

    @Test
    public void testGenerateResetPasswordEmail() {
        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/resetPassword")
                .request(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testGetAvatar() {
        User user = mock(User.class);
        Map<String, Object> properties = new HashMap<>();
        properties.put("avatar", TEST_AVATAR_URI);

        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        when(user.getProperties()).thenReturn(properties);
        when(user.getDomain()).thenReturn(TEST_DOMAIN_ID);

        Response response = RULE.client().target("/v1.0/" + TEST_DOMAIN_ID + "/user/" + TEST_USER_ID + "/avatar")
                .property(ClientProperties.FOLLOW_REDIRECTS, false).request(MediaType.APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.TEMPORARY_REDIRECT.getStatusCode());
        assertThat(java.util.Optional.ofNullable(response.getHeaders().get("Location"))
                .map(locations -> locations.contains(TEST_AVATAR_URI)).orElse(false)).isTrue();
    }

}
