package io.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import io.corbel.iam.model.Device;
import io.corbel.iam.model.DeviceResponse;
import io.corbel.iam.service.DeviceService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.IdentityService;
import io.corbel.iam.service.UserService;
import io.corbel.lib.queries.exception.MalformedJsonQueryException;
import io.corbel.lib.queries.parser.AggregationParser;
import io.corbel.lib.queries.parser.PaginationParser;
import io.corbel.lib.queries.parser.QueryParametersParser;
import io.corbel.lib.queries.parser.QueryParser;
import io.corbel.lib.queries.parser.SearchParser;
import io.corbel.lib.queries.parser.SortParser;
import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.JsonAggregationResultsFactory;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.auth.AuthorizationInfoProvider;
import io.corbel.lib.ws.auth.AuthorizationRequestFilter;
import io.corbel.lib.ws.queries.QueryParametersProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.google.gson.JsonElement;

/**
 * @author Alexander De Leon
 *
 */
public class UserResourceDevicesTest extends UserResourceTestBase {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;

    private static final SortParser sortParserMock = mock(SortParser.class);
    private static final SearchParser searchParserMock = mock(SearchParser.class);
    private static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    private static final PaginationParser paginationParserMock = mock(PaginationParser.class);
    private static final QueryParser queryParserMock = mock(QueryParser.class);

    private static final UserService userServiceMock = mock(UserService.class);
    private static final DomainService domainServiceMock = mock(DomainService.class);
    private static final IdentityService identityServiceMock = mock(IdentityService.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final DeviceService devicesServiceMock = mock(DeviceService.class);
    private static final AuthorizationInfoProvider authorizationInfoProvider = new AuthorizationInfoProvider();
    private static final String TEST_DEVICE_NAME = "My device name";
    private static final String TEST_DEVICE_URI = "Test device URI";
    private static final String TEST_DEVICE_ID = "TestDeviceID";
    private static final String TEST_DEVICE_UID = "TestDeviceUID";
    private static final String TEST_DOMAIN_ID = "domain";
    private static final String TEST_OTHER_DOMAIN_ID = "otherDomain";


    private static final Authenticator<String, AuthorizationInfo> authenticator = mock(Authenticator.class);
    private static final String TEST_DEVICE_TYPE = "Android";
    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticator, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, "", false, "user"));
    private static AggregationResultsFactory<JsonElement> aggregationResultsFactory = new JsonAggregationResultsFactory();

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addResource(
                    new UserResource(userServiceMock, domainServiceMock, identityServiceMock, devicesServiceMock,
                            aggregationResultsFactory, FIXED_CLOCK))
            .addProvider(filter)
            .addProvider(authorizationInfoProvider.getBinder())
            .addProvider(
                    new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, new QueryParametersParser(queryParserMock,
                            aggregationParserMock, sortParserMock, paginationParserMock, searchParserMock)).getBinder())
            .addProvider(GenericExceptionMapper.class).build();

    public UserResourceDevicesTest() throws Exception {}

    @Before
    public void setUp() throws AuthenticationException {
        reset(userServiceMock, domainServiceMock, identityServiceMock, devicesServiceMock, authorizationInfoMock);
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(authorizationInfoMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(authenticator.authenticate(any())).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
        when(domainServiceMock.getDomain(TEST_DOMAIN_ID)).thenReturn(Optional.ofNullable(TEST_DOMAIN));
    }

    @Override
    protected ResourceTestRule getTestRule() {
        return RULE;
    }

    @Test
    public void testPutDevice() {
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID).setUid(TEST_DEVICE_UID);
        Device createdDevice = new Device();
        createdDevice.setUid(TEST_DEVICE_UID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.update(device, false)).thenReturn(createdDevice);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_UID).put(
                Entity.json(device), Response.class);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEST_DEVICE_UID);
        verify(devicesServiceMock, only()).update(device, false);
    }

    @Test
    public void testPutDeviceWithConnectionDate() {
        when(authorizationInfoMock.getDeviceId()).thenReturn(TEST_DEVICE_UID);

        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID).setUid(TEST_DEVICE_UID);
        Device createdDevice = new Device();
        createdDevice.setUid(TEST_DEVICE_UID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.update(device, true)).thenReturn(createdDevice);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_UID).put(
                Entity.json(device), Response.class);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEST_DEVICE_UID);
        verify(devicesServiceMock, only()).update(device, true);
    }

    @Test
    public void testPutDeviceAdminInOtherDomain() {
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_OTHER_DOMAIN_ID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_OTHER_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_UID).put(
                Entity.json(device), Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testPutDeviceNotUserExist() {
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_UID).put(
                Entity.json(device), Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testGetDevice() {
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE).setUid(TEST_DEVICE_UID)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.getByUidAndUserId(TEST_DEVICE_UID, TEST_USER_ID, TEST_DOMAIN_ID)).thenReturn(device);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_UID).get(
                Response.class);
        assertThat(response.getStatus()).isEqualTo(200);
        DeviceResponse deviceResponse = response.readEntity(DeviceResponse.class);
        assertThat(deviceResponse.getId()).isEqualTo(TEST_DEVICE_UID);
        assertThat(deviceResponse.getName()).isEqualTo(TEST_DEVICE_NAME);
        assertThat(deviceResponse.getNotificationUri()).isEqualTo(TEST_DEVICE_URI);
        assertThat(deviceResponse.isNotificationEnabled()).isEqualTo(true);
        verify(devicesServiceMock, only()).getByUidAndUserId(TEST_DEVICE_UID, TEST_USER_ID, TEST_DOMAIN_ID);
    }

    @Test
    public void testGetDeviceInOtherDomain() {
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_OTHER_DOMAIN_ID);
        when(devicesServiceMock.getByUidAndUserId(TEST_DEVICE_ID, TEST_USER_ID, TEST_DOMAIN_ID)).thenReturn(device);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_OTHER_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_ID).get(
                Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testGetDeviceNotFound() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_ID).get(
                Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }


    @Test
    public void testGetUserDevices() throws MalformedJsonQueryException {
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(TEST_DEVICE_TYPE)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        List<Device> devicesList = Collections.singletonList(device);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.getByUserId(eq(TEST_USER_ID), any())).thenReturn(devicesList);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device").get(Response.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(new GenericType<List<Device>>() {})).isEqualTo(devicesList);
        verify(devicesServiceMock, only()).getByUserId(eq(TEST_USER_ID), any());
    }

    @Test
    public void testGetUserDevicesInOtherDomain() {
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_OTHER_DOMAIN_ID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_OTHER_DOMAIN_ID + "/user/me/device").get(Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testGetUserDevicesNotFound() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device").get(Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testDeleteDevice() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_ID).delete(
                Response.class);
        assertThat(response.getStatus()).isEqualTo(204);
        verify(devicesServiceMock, only()).deleteByUidAndUserId(TEST_DEVICE_ID, TEST_USER_ID, TEST_DOMAIN_ID);
    }

    @Test
    public void testDeleteDeviceInOtherDomain() {
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_OTHER_DOMAIN_ID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_OTHER_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_ID).delete(
                Response.class);
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testDeleteDeviceUserNotFound() {
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/" + TEST_DOMAIN_ID + "/user/me/device/" + TEST_DEVICE_ID).delete(
                Response.class);
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }

}
