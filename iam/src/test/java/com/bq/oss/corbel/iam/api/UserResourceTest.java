package com.bq.oss.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Clock;
import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.bq.oss.lib.queries.parser.SortParser;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bq.oss.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import com.bq.oss.corbel.iam.exception.IdentityAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.UserProfileConfigurationException;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.model.UserWithIdentity;
import com.bq.oss.corbel.iam.repository.CreateUserException;
import com.bq.oss.corbel.iam.service.DeviceService;
import com.bq.oss.corbel.iam.service.DomainService;
import com.bq.oss.corbel.iam.service.IdentityService;
import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.queries.builder.ResourceQueryBuilder;
import com.bq.oss.lib.queries.exception.MalformedJsonQueryException;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.request.*;
import com.bq.oss.lib.ws.api.error.GenericExceptionMapper;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.AuthorizationInfoProvider;
import com.bq.oss.lib.ws.model.Error;
import com.bq.oss.lib.ws.queries.QueryParametersProvider;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Alexander De Leon
 * 
 */
public class UserResourceTest extends UserResourceTestBase {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;
    private static final String TEST_AVATAR_URI = "http://jklsdfjklasdfjkl.com/jsdklfjasdkl.png";


    private static final SortParser sortParserMock = mock(SortParser.class);
    private static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    private static final UserService userServiceMock = mock(UserService.class);
    private static final DomainService domainServiceMock = mock(DomainService.class);
    private static final IdentityService identityServiceMock = mock(IdentityService.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final QueryParser queryParserMock = mock(QueryParser.class);
    private static final DeviceService devicesServiceMock = mock(DeviceService.class);
    private static final AuthorizationInfoProvider authorizationInfoProviderSpy = spy(new AuthorizationInfoProvider());

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new UserResource(userServiceMock, domainServiceMock, identityServiceMock, devicesServiceMock, Clock.systemUTC()))
            .addProvider(authorizationInfoProviderSpy)
            .addProvider(new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, queryParserMock, aggregationParserMock, sortParserMock))
            .addProvider(GenericExceptionMapper.class).build();

    public UserResourceTest() throws Exception {
        when(authorizationInfoMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        doReturn(authorizationInfoMock).when(authorizationInfoProviderSpy).getValue(any());
    }

    @Override
    protected ResourceTestRule getTestRule() {
        return RULE;
    }

    @Before
    public void setUp() {
        reset(userServiceMock, domainServiceMock, identityServiceMock);

        when(domainServiceMock.getDomain(TEST_DOMAIN_ID)).thenReturn(Optional.ofNullable(TEST_DOMAIN));
    }

    @Test
    public void testAddUser() throws CreateUserException {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);

        User user = getTestUser();
        User userWithCreationDate = getTestUser();
        userWithCreationDate.setCreatedDate(new Date());
        when(userServiceMock.create(Mockito.any(User.class))).thenReturn(userWithCreationDate);

        ClientResponse response = addUserClient().post(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaders().getFirst("Location")).contains(TEST_USER_ID);
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
        ClientResponse response = addUserClient().post(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity(Error.class).getError()).isEqualTo("invalid_argument");
    }

    @SuppressWarnings("unused")
    @Test
    public void testUserAlreadyExists() throws CreateUserException {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        User user = getTestUser();
        user.setId(null);
        when(userServiceMock.create(any())).thenThrow(CreateUserException.class);

        ClientResponse response = addUserClient().post(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getEntity(Error.class).getError()).isEqualTo("entity_exists");
    }

    @Test
    public void testMissingUsername() {
        User user = createTestUser();
        user.setUsername(null);
        ClientResponse response = addUserClient().post(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void testMissingEmail() {
        User user = createTestUser();
        user.setEmail(null);
        ClientResponse response = addUserClient().post(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void testScopeNotAllowed() throws CreateUserException {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(false);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(userServiceMock.create(Mockito.eq(removeId(createTestUser())))).thenReturn(user);
        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    public void testGetUser() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        ClientResponse response = getUserClient(TEST_USER_ID).get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity(User.class)).isEqualsToByComparingFields(user);
    }

    @Test
    public void testGetUserAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";
        Aggregation operation = new Count("*");
        ResourceQuery resourceQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(resourceQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        CountResult expectedResult = new CountResult();
        expectedResult.setCount(4);
        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        when(userServiceMock.countUsersByDomain(eq(TEST_DOMAIN_ID), eq(resourceQuery))).thenReturn(expectedResult);

        ClientResponse response = getTestRule().client().resource("/v1.0/user/")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity(CountResult.class)).isEqualsToByComparingFields(expectedResult);
    }

    @Test
    public void testGetUserInvalidAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";
        class OtherAggretation implements Aggregation {
            @Override
            public ResourceQuery operate(ResourceQuery resourceQuery) {
                return null;
            }

            @Override
            public AggregationOperator getOperator() {
                return null;
            }
        };
        Aggregation operation = new OtherAggretation();
        ResourceQuery resourceQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(resourceQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        ClientResponse response = getTestRule().client().resource("/v1.0/user")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetUserMe() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        ClientResponse response = getUserClientMe().get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity(User.class)).isEqualsToByComparingFields(user);
    }

    @Test
    public void testGetUserMeOutOfDomain() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn("other_domain");
        ClientResponse response = getUserClientMe().get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteUser() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        ClientResponse response = getUserClient(TEST_USER_ID).delete(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(204);
        verify(userServiceMock).delete(user);
        verify(identityServiceMock).deleteUserIdentities(user);
        verify(devicesServiceMock).deleteByUserId(user);
    }

    @Test
    public void testDeleteMissingUser() {
        when(userServiceMock.findUserDomain("fakeId")).thenReturn(null);
        ClientResponse response = getUserClient("fakeId").delete(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(204);
        verifyNoMoreInteractions(identityServiceMock, devicesServiceMock);
        verify(userServiceMock, never()).delete(any());
    }

    @Test
    public void testGetUserOutOfDomain() {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_OTHER_DOMAIN);
        ClientResponse response = getUserClient(TEST_USER_ID).get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteUserOutOfDomain() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(new User());
        ClientResponse response = getUserClient(TEST_USER_ID).delete(ClientResponse.class);
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

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testUpdateEmail() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        String newEmail = "new_email@test";
        User user = new User();
        user.setEmail(newEmail);

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userServiceMock).update(userCaptor.capture());

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(newEmail);
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

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
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

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
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

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
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

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    public void testSignOutMe() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);

        ClientResponse response = RULE.client().resource("/v1.0/user/me/signout").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).put(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).signOut(TEST_USER_ID, java.util.Optional.of(TEST_TOKEN));
    }

    @Test
    public void testDisconnectMe() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);

        ClientResponse response = RULE.client().resource("/v1.0/user/me/disconnect").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).put(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).signOut(TEST_USER_ID);
    }

    @Test
    public void testDisconnect() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        ClientResponse response = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/disconnect").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).put(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(204);

        verify(userServiceMock).signOut(TEST_USER_ID);
    }

    @Test
    public void testDisconnectNotFound() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(null);

        ClientResponse response = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/disconnect").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).put(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testAddProperty() {
        when(domainServiceMock.scopesAllowedInDomain(TEST_SCOPES, TEST_DOMAIN)).thenReturn(true);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(getTestUser());

        User user = new User();
        user.addProperty("a", "1");

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
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

        ClientResponse response = getUserClient(TEST_USER_ID).put(ClientResponse.class, user);
        assertThat(response.getStatus()).isEqualTo(204);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(201);

        ArgumentCaptor<Identity> captor = ArgumentCaptor.forClass(Identity.class);
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/me/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(201);

        ArgumentCaptor<Identity> captor = ArgumentCaptor.forClass(Identity.class);
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/me/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/me/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
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

        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/me/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
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
        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(400);
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
        ClientResponse clientResponse = RULE.client().resource("/v1.0/user/me/identity").type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, identity);

        assertThat(clientResponse.getStatus()).isEqualTo(400);
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

        ClientResponse response = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/identity").accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity(new GenericType<List<Identity>>() {})).isEqualTo(identities);
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

        ClientResponse response = RULE.client().resource("/v1.0/user/me/identity").accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity(new GenericType<List<Identity>>() {})).isEqualTo(identities);
    }

    @Test
    public void testMeGetIdentitiesWithoutUser() {
        when(authorizationInfoMock.getUserId()).thenReturn(null);

        ClientResponse response = RULE.client().resource("/v1.0/user/me/identity").accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

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

        ClientResponse response = getUserProfile(TEST_USER_ID).get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(200);

        User userResponse = response.getEntity(User.class);
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
        ResourceQuery resourceQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(resourceQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        CountResult expectedResult = new CountResult();
        expectedResult.setCount(4);
        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        when(userServiceMock.countUsersByDomain(eq(TEST_DOMAIN_ID), eq(resourceQuery))).thenReturn(expectedResult);

        ClientResponse response = getTestRule().client().resource("/v1.0/user/profile")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity(CountResult.class)).isEqualsToByComparingFields(expectedResult);
    }

    @Test
    public void testGetUserProfileInvalidAggregation() throws UnsupportedEncodingException, MalformedJsonQueryException {
        String aggRequest = "{\"$count\":\"*\"}";
        String queryString = "queryString";
        class OtherAggretation implements Aggregation {
            @Override
            public ResourceQuery operate(ResourceQuery resourceQuery) {
                return null;
            }

            @Override
            public AggregationOperator getOperator() {
                return null;
            }
        };
        Aggregation operation = new OtherAggretation();
        ResourceQuery resourceQuery = new ResourceQuery();
        when(queryParserMock.parse(queryString)).thenReturn(resourceQuery);
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);

        when(aggregationParserMock.parse(aggRequest)).thenReturn(operation);

        ClientResponse response = getTestRule().client().resource("/v1.0/user/profile")
                .queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetNotFoundUserProfile() throws UserProfileConfigurationException {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);

        when(userServiceMock.getUserProfile(user, TEST_DOMAIN.getUserProfileFields())).thenReturn(null);

        ClientResponse response = getUserProfile(TEST_USER_ID).get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetUserProfileWithException() throws UserProfileConfigurationException {
        when(userServiceMock.findUserDomain(TEST_USER_ID)).thenReturn(TEST_DOMAIN_ID);
        User user = createTestUser();
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);

        when(userServiceMock.getUserProfile(user, TEST_DOMAIN.getUserProfileFields())).thenThrow(UserProfileConfigurationException.class);

        ClientResponse response = getUserProfile(TEST_USER_ID).get(ClientResponse.class);
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void testGetProfilesWithInvalidQuery() throws MalformedJsonQueryException, UniformInterfaceException, ClientHandlerException,
            UnsupportedEncodingException, UserProfileConfigurationException {
        TEST_DOMAIN.setUserProfileFields(Sets.newHashSet("field1"));

        String queryString = "queryString";
        ResourceQuery resourceQuery = new ResourceQueryBuilder().add("field1", "value1").add("field2", "value2").build();

        when(queryParserMock.parse(queryString)).thenReturn(resourceQuery);
        ClientResponse response = getTestRule().client().resource("/v1.0/user/profile")
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        ArgumentCaptor<ResourceQuery> argument = ArgumentCaptor.forClass(ResourceQuery.class);
        verify(userServiceMock).findUserProfilesByDomain(eq(UserResourceTestBase.TEST_DOMAIN), argument.capture(), any(), any());

        assertThat(argument.getValue().iterator().next().getField()).isEqualTo("_notExistent");
    }

    @Test
    public void testGetProfilesWithValidQuery() throws MalformedJsonQueryException, UniformInterfaceException, ClientHandlerException,
            UnsupportedEncodingException, UserProfileConfigurationException {
        TEST_DOMAIN.setUserProfileFields(Sets.newHashSet("field1"));

        String queryString = "queryString";
        ResourceQuery resourceQuery = new ResourceQueryBuilder().add("field1", "value1").build();

        when(queryParserMock.parse(queryString)).thenReturn(resourceQuery);
        ClientResponse response = getTestRule().client().resource("/v1.0/user/profile")
                .queryParam("api:query", URLEncoder.encode(queryString, "UTF-8")).type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);
        ArgumentCaptor<ResourceQuery> argument = ArgumentCaptor.forClass(ResourceQuery.class);
        verify(userServiceMock).findUserProfilesByDomain(eq(UserResourceTestBase.TEST_DOMAIN), argument.capture(), any(), any());

        assertThat(argument.getValue().iterator().next().getField()).isEqualTo("field1");
    }

    @Test
    public void testGenerateResetPasswordEmail() {
        ClientResponse response = RULE.client().resource("/v1.0/user/resetPassword").type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testGetAvatar() {
        User user = mock(User.class);
        Map<String, Object> properties = new HashMap<>();
        properties.put("avatar", TEST_AVATAR_URI);

        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(user);
        when(user.getProperties()).thenReturn(properties);
        when(user.getDomain()).thenReturn(TEST_DOMAIN_ID);

        ClientResponse response = RULE.client().resource("/v1.0/user/" + TEST_USER_ID + "/avatar").type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.TEMPORARY_REDIRECT.getStatusCode());
        assertThat(
                java.util.Optional.ofNullable(response.getHeaders().get("Location")).map(locations -> locations.contains(TEST_AVATAR_URI))
                        .orElse(false)).isTrue();
    }

}
