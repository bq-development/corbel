package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.DeviceIdGenerator;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.DeviceRepository;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

/**
 * @author Francisco Sánchez
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultDeviceServiceTest {

    private static final String TEST_DEVICE_ID = "TEST_DOMAIN:TEST_USER_ID:TEST_UID";
    private static final String TEST_USER_ID = "TEST_USER_ID";
    private static final String TEST_UID = "TEST_UID";
    private static final String TEST_DOMAIN = "TEST_DOMAIN";
    private static final String TEST_OTHER_USER_ID = "TEST_OTHER_USER_ID";

    private DefaultDeviceService deviceService;

    @Mock private DeviceRepository deviceRepositoryMock;
    @Mock private Device deviceMock;
    @Mock private DeviceIdGenerator deviceIdGeneratorMock;
    @Mock private EventsService eventsServiceMock;
    private Instant now = Instant.now();

    @Before
    public void setUp() {
        deviceService = new DefaultDeviceService(deviceRepositoryMock, deviceIdGeneratorMock, eventsServiceMock,
                Clock.fixed(now, ZoneId.systemDefault()));
    }

    @Test
    public void testGetDeviceById() {
        when(deviceRepositoryMock.findOne(TEST_DEVICE_ID)).thenReturn(deviceMock);

        Device device = deviceService.get(TEST_DEVICE_ID);

        assertThat(deviceMock).isEqualTo(device);
    }

    @Test
    public void testGetDeviceByIdAndUserId() {
        when(deviceRepositoryMock.findById(anyString())).thenReturn(deviceMock);

        Device device = deviceService.getByUidAndUserId(TEST_UID, TEST_USER_ID, TEST_DOMAIN);

        assertThat(deviceMock).isEqualTo(device);
    }

    @Test
    public void testGetDeviceByUserIdWithNullQuery() {
        QueryParameters queryParametersMock = mock(QueryParameters.class);
        Pagination paginationMock = mock(Pagination.class);
        Sort sortMock = mock(Sort.class);
        when(queryParametersMock.getQueries()).thenReturn(Optional.empty());
        when(queryParametersMock.getPagination()).thenReturn(paginationMock);
        when(queryParametersMock.getSort()).thenReturn(Optional.of(sortMock));
        List<Device> devicesMockList = new LinkedList<>();
        devicesMockList.add(mock(Device.class));

        List<ResourceQuery> resourceQueriesTransformed = new LinkedList<>();
        resourceQueriesTransformed.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_USER_ID).build());
        when(deviceRepositoryMock.find(resourceQueriesTransformed, paginationMock, sortMock)).thenReturn(devicesMockList);

        List<Device> device = deviceService.getByUserId(TEST_USER_ID, queryParametersMock);

        assertThat(device).isEqualTo(devicesMockList);
    }


    @Test
    public void testGetDeviceByUserIdWithTypeQuery() {
        QueryParameters queryParametersMock = mock(QueryParameters.class);
        List<ResourceQuery> resourceQueriesSend = new LinkedList<>();
        resourceQueriesSend.add(new ResourceQueryBuilder().add("type", "ANDROID").build());
        Pagination paginationMock = mock(Pagination.class);
        Sort sortMock = mock(Sort.class);
        when(queryParametersMock.getQueries()).thenReturn(Optional.of(resourceQueriesSend));
        when(queryParametersMock.getPagination()).thenReturn(paginationMock);
        when(queryParametersMock.getSort()).thenReturn(Optional.of(sortMock));
        List<Device> devicesMockList = new LinkedList<>();
        devicesMockList.add(mock(Device.class));

        List<ResourceQuery> resourceQueriesTransformed = new LinkedList<>();
        resourceQueriesTransformed.add(new ResourceQueryBuilder().add("type", "ANDROID").add(Device.USER_ID_FIELD, TEST_USER_ID).build());
        when(deviceRepositoryMock.find(resourceQueriesTransformed, paginationMock, sortMock)).thenReturn(devicesMockList);

        List<Device> device = deviceService.getByUserId(TEST_USER_ID, queryParametersMock);

        assertThat(device).isEqualTo(devicesMockList);
    }

    @Test
    public void testGetDeviceByUserIdWithOtherUserSearch() {
        QueryParameters queryParametersMock = mock(QueryParameters.class);
        List<ResourceQuery> resourceQueriesSended = new LinkedList<>();
        resourceQueriesSended.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_OTHER_USER_ID).build());
        Pagination paginationMock = mock(Pagination.class);
        Sort sortMock = mock(Sort.class);
        when(queryParametersMock.getQueries()).thenReturn(Optional.of(resourceQueriesSended));
        when(queryParametersMock.getPagination()).thenReturn(paginationMock);
        when(queryParametersMock.getSort()).thenReturn(Optional.of(sortMock));
        List<Device> devicesMockList = new LinkedList<>();
        devicesMockList.add(mock(Device.class));

        List<ResourceQuery> resourceQueriesTransformed = new LinkedList<>();
        resourceQueriesTransformed.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_USER_ID).build());
        when(deviceRepositoryMock.find(resourceQueriesTransformed, paginationMock, sortMock)).thenReturn(devicesMockList);

        List<Device> device = deviceService.getByUserId(TEST_USER_ID, queryParametersMock);

        assertThat(device).isEqualTo(devicesMockList);
    }


    @Test
    public void testGetDeviceByMultipleUserIdWithOtherUserSearch() {
        QueryParameters queryParametersMock = mock(QueryParameters.class);
        List<ResourceQuery> resourceQueriesSended = new LinkedList<>();
        resourceQueriesSended.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_OTHER_USER_ID).build());
        resourceQueriesSended.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_OTHER_USER_ID).build());
        Pagination paginationMock = mock(Pagination.class);
        Sort sortMock = mock(Sort.class);
        when(queryParametersMock.getQueries()).thenReturn(Optional.of(resourceQueriesSended));
        when(queryParametersMock.getPagination()).thenReturn(paginationMock);
        when(queryParametersMock.getSort()).thenReturn(Optional.of(sortMock));
        List<Device> devicesMockList = new LinkedList<>();
        devicesMockList.add(mock(Device.class));

        List<ResourceQuery> resourceQueriesTransformed = new LinkedList<>();
        resourceQueriesTransformed.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_USER_ID).build());
        resourceQueriesTransformed.add(new ResourceQueryBuilder().add(Device.USER_ID_FIELD, TEST_USER_ID).build());
        when(deviceRepositoryMock.find(resourceQueriesTransformed, paginationMock, sortMock)).thenReturn(devicesMockList);

        List<Device> device = deviceService.getByUserId(TEST_USER_ID, queryParametersMock);

        assertThat(device).isEqualTo(devicesMockList);
    }

    @Test
    public void testAddDeviceByIdAndUserId() {
        Device deviceToAdd = new Device();
        deviceToAdd.setId(TEST_DEVICE_ID);
        when(deviceIdGeneratorMock.generateId(deviceToAdd)).thenReturn(TEST_DEVICE_ID);
        when(deviceRepositoryMock.upsert(TEST_DEVICE_ID, deviceToAdd)).thenReturn(false);

        Device device = deviceService.update(deviceToAdd);

        assertThat(deviceToAdd).isEqualTo(device);
        assertThat(deviceToAdd.getFirstConnection()).isEqualTo(new Date(now.toEpochMilli()));
        verify(eventsServiceMock).sendDeviceCreateEvent(device);
    }

    @Test
    public void testUpdateDeviceByIdAndUserId() {
        Device deviceToUpdate = new Device();
        deviceToUpdate.setId(TEST_DEVICE_ID);
        when(deviceIdGeneratorMock.generateId(deviceToUpdate)).thenReturn(TEST_DEVICE_ID);
        when(deviceRepositoryMock.upsert(TEST_DEVICE_ID, deviceToUpdate)).thenReturn(true);

        Device device = deviceService.update(deviceToUpdate);

        assertThat(deviceToUpdate).isEqualTo(device);
        assertThat(deviceToUpdate.getFirstConnection()).isNull();
        verify(eventsServiceMock).sendDeviceUpdateEvent(device);
    }

    @Test
    public void testDeleteByIdAndUserId() {
        when(deviceRepositoryMock.deleteById(anyString())).thenReturn(1L);

        deviceService.deleteByUidAndUserId(TEST_UID, TEST_USER_ID, TEST_DOMAIN);

        verify(deviceRepositoryMock).deleteById(TEST_DEVICE_ID);
        verify(eventsServiceMock).sendDeviceDeleteEvent(TEST_UID, TEST_USER_ID, TEST_DOMAIN);
    }

    @Test
    public void testDeleteByIdAndUserIdNotExist() {
        when(deviceRepositoryMock.deleteById(anyString())).thenReturn(0L);

        deviceService.deleteByUidAndUserId(TEST_UID, TEST_USER_ID, TEST_DOMAIN);

        verify(deviceRepositoryMock).deleteById(TEST_DEVICE_ID);
        verify(eventsServiceMock, never()).sendDeviceDeleteEvent(TEST_DEVICE_ID, TEST_USER_ID, TEST_DOMAIN);
    }

    @Test
    public void testDeleteByUserId() {
        List<Device> deviceMocks = Collections.singletonList(deviceMock);
        when(deviceRepositoryMock.deleteByUserId(TEST_USER_ID)).thenReturn(deviceMocks);

        User user = new User();
        user.setId(TEST_USER_ID);
        List<Device> devices = deviceService.deleteByUserId(user);

        verify(deviceRepositoryMock).deleteByUserId(TEST_USER_ID);
        assertThat(deviceMocks).isEqualTo(devices);
    }

}
