package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.DeviceIdGenerator;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.DeviceRepository;

/**
 * Created by Francisco Sanchez on 15/02/16.
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultDeviceServiceTest {
    private static final String TEST_DEVICE_ID = "TEST_DOMAIN:TEST_USER_ID:TEST_UID";
    private static final String TEST_USER_ID = "TEST_USER_ID";
    private static final String TEST_UID = "TEST_UID";
    private static final String TEST_DOMAIN = "TEST_DOMAIN";

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
    public void testAddDeviceByIdAndUserId() {
        Device deviceToAdd = new Device();
        deviceToAdd.setId(TEST_DEVICE_ID);
        when(deviceIdGeneratorMock.generateId(deviceToAdd)).thenReturn(TEST_DEVICE_ID);
        when(deviceRepositoryMock.upsert(TEST_DEVICE_ID, deviceToAdd)).thenReturn(false);

        Device device = deviceService.update(deviceToAdd);

        assertThat(deviceToAdd).isEqualTo(device);
        assertThat(deviceToAdd.getCreatedAt()).isEqualTo(new Date(now.toEpochMilli()));
        assertThat(deviceToAdd.getUpdatedAt()).isEqualTo(new Date(now.toEpochMilli()));
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
        assertThat(deviceToUpdate.getCreatedAt()).isNull();
        assertThat(deviceToUpdate.getUpdatedAt()).isEqualTo(new Date(now.toEpochMilli()));
        verify(eventsServiceMock).sendDeviceUpdateEvent(device);
    }

    @Test
    public void testDeleteByIdAndUserId() {
        when(deviceRepositoryMock.deleteById(anyString())).thenReturn(1L);

        deviceService.deleteByUidAndUserId(TEST_UID, TEST_USER_ID, TEST_DOMAIN);

        verify(deviceRepositoryMock).deleteById(TEST_DEVICE_ID);
        verify(eventsServiceMock).sendDeviceDeleteEvent(TEST_DEVICE_ID, TEST_USER_ID, TEST_DOMAIN);
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
        List<Device> deviceMocks = Arrays.asList(deviceMock);
        when(deviceRepositoryMock.deleteByUserId(TEST_USER_ID)).thenReturn(deviceMocks);

        User user = new User();
        user.setId(TEST_USER_ID);
        List<Device> devices = deviceService.deleteByUserId(user);

        verify(deviceRepositoryMock).deleteByUserId(TEST_USER_ID);
        assertThat(deviceMocks).isEqualTo(devices);
    }

}
