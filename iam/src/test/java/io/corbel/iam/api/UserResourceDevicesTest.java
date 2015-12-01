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
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.time.Clock;
import java.util.Arrays;
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

import io.corbel.iam.model.Device;
import io.corbel.iam.service.DeviceService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.IdentityService;
import io.corbel.iam.service.UserService;
import io.corbel.lib.queries.parser.AggregationParser;
import io.corbel.lib.queries.parser.PaginationParser;
import io.corbel.lib.queries.parser.QueryParametersParser;
import io.corbel.lib.queries.parser.QueryParser;
import io.corbel.lib.queries.parser.SearchParser;
import io.corbel.lib.queries.parser.SortParser;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.auth.AuthorizationInfoProvider;
import io.corbel.lib.ws.auth.AuthorizationRequestFilter;
import io.corbel.lib.ws.queries.QueryParametersProvider;

/**
 * @author Alexander De Leon
 *
 */
public class UserResourceDevicesTest extends UserResourceTestBase {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;


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
    private static final AuthorizationInfoProvider authorizationInfoProvider = new AuthorizationInfoProvider();
    private static final String TEST_DEVICE_NAME = "My device name";
    private static final String TEST_DEVICE_URI = "Test device URI";
    private static final String TEST_DEVICE_ID = "TestDeviceID";
    private static final String TEST_DEVICE_UID = "TestDeviceUID";

    private static final Authenticator<String, AuthorizationInfo> authenticator = mock(Authenticator.class);
    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticator, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, "",false));

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addResource(new UserResource(userServiceMock, domainServiceMock, identityServiceMock, devicesServiceMock, Clock.systemUTC()))
            .addProvider(filter)
            .addProvider(authorizationInfoProvider.getBinder())
            .addProvider(
                    new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, new QueryParametersParser(queryParserMock,
                            aggregationParserMock, sortParserMock, paginationParserMock, searchParserMock)).getBinder())
            .addProvider(GenericExceptionMapper.class).build();

    public UserResourceDevicesTest() throws Exception {
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(authorizationInfoMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        when(authenticator.authenticate(any())).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
    }

    @Before
    public void setUp() {
        reset(userServiceMock, domainServiceMock, identityServiceMock, devicesServiceMock);
        when(domainServiceMock.getDomain(TEST_DOMAIN_ID)).thenReturn(Optional.ofNullable(TEST_DOMAIN));
    }

    @Override
    protected ResourceTestRule getTestRule() {
        return RULE;
    }


    @Test
    public void testPutDevice() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID).setUid(TEST_DEVICE_UID);
        Device createdDevice = new Device();
        createdDevice.setId(TEST_DEVICE_ID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.update(device)).thenReturn(createdDevice);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices").put(Entity.json(device), Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEST_DEVICE_ID);
        verify(devicesServiceMock, only()).update(device);
    }

    @Test
    public void testPutDeviceAdminInOtherDomain() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID + "OTHER");
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices").put(Entity.json(device), Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testPutDeviceNotUserExist() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI).setUid(TEST_DEVICE_UID);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices").put(Entity.json(device), Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }


    @Test
    public void testGetDevice() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.getByIdAndUserId(TEST_DEVICE_ID, TEST_USER_ID)).thenReturn(device);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices/" + TEST_DEVICE_ID).get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Device.class)).isEqualsToByComparingFields(device);
        verify(devicesServiceMock, only()).getByIdAndUserId(TEST_DEVICE_ID, TEST_USER_ID);
    }

    @Test
    public void testGetDeviceInOtherDomain() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID + "OTHER");
        when(devicesServiceMock.getByIdAndUserId(TEST_DEVICE_ID, TEST_USER_ID)).thenReturn(device);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices/" + TEST_DEVICE_ID).get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testGetDeviceNotFound() {
        // CONFIGURE
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices/" + TEST_DEVICE_ID).get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }


    @Test
    public void testGetUserDevices() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        List<Device> devicesList = Arrays.asList(device);
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        when(devicesServiceMock.getByUserId(TEST_USER_ID)).thenReturn(devicesList);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices").get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(new GenericType<List<Device>>() {})).isEqualTo(devicesList);
        verify(devicesServiceMock, only()).getByUserId(TEST_USER_ID);
    }

    @Test
    public void testGetUserDevicesInOtherDomain() {
        // CONFIGURE
        Device device = new Device().setDomain(TEST_DOMAIN_ID).setName(TEST_DEVICE_NAME).setType(Device.Type.Android)
                .setNotificationEnabled(true).setNotificationUri(TEST_DEVICE_URI);
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID + "OTHER");
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());

        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices").get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testGetUserDevicesNotFound() {
        // CONFIGURE
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices").get(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DELETE /v1.0/{userId}/me/devices/{deviceId}
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void testDeleteDevice() {
        // CONFIGURE
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices/" + TEST_DEVICE_ID).delete(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(204);
        verify(devicesServiceMock, only()).deleteByIdAndUserId(TEST_DEVICE_ID, TEST_USER_ID, TEST_DOMAIN_ID);
    }

    @Test
    public void testDeleteDeviceInOtherDomain() {
        // CONFIGURE
        when(authorizationInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID + "OTHER");
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(createTestUser());
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices/" + TEST_DEVICE_ID).delete(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoMoreInteractions(devicesServiceMock);
    }

    @Test
    public void testDeleteDeviceUserNotFound() {
        // CONFIGURE
        when(userServiceMock.findById(TEST_USER_ID)).thenReturn(null);
        // LAUNCH
        Response response = apiCall("Bearer " + TEST_TOKEN, "/v1.0/user/me/devices/" + TEST_DEVICE_ID).delete(Response.class);
        // VERIFY
        assertThat(response.getStatus()).isEqualTo(404);
        verifyNoMoreInteractions(devicesServiceMock);
    }

}
