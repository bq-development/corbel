package io.corbel.iam.eventbus;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.event.DeviceEvent;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;

/**
 * Created by Francisco Sanchez on 18/02/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceDeletedEventHandlerTest {

    private DeviceDeletedEventHandler deviceDeletedEventHandler;

    @Mock
    private AuthorizationRulesRepository authorizationRulesRepositoryMock;
    @Mock
    private UserTokenRepository userTokenRepositoryMock;

    @Before
    public void setup() {
        reset(authorizationRulesRepositoryMock, userTokenRepositoryMock);
        deviceDeletedEventHandler = new DeviceDeletedEventHandler(authorizationRulesRepositoryMock, userTokenRepositoryMock);
    }

    @Test
    public void testHandleNotDeleteEvent() throws Exception {
        deviceDeletedEventHandler.handle(new DeviceEvent(DeviceEvent.Type.CREATED, null, null, null));
        verifyNoMoreInteractions(authorizationRulesRepositoryMock, userTokenRepositoryMock);
        deviceDeletedEventHandler.handle(new DeviceEvent(DeviceEvent.Type.UPDATED, null, null, null));
        verifyNoMoreInteractions(authorizationRulesRepositoryMock, userTokenRepositoryMock);
        deviceDeletedEventHandler.handle(new DeviceEvent(null, null, null, null));
        verifyNoMoreInteractions(authorizationRulesRepositoryMock, userTokenRepositoryMock);
    }

    @Test
    public void testHandleDeleteEvent() throws Exception {
        DeviceEvent deviceEventMock = mock(DeviceEvent.class);
        when(deviceEventMock.getType()).thenReturn(DeviceEvent.Type.DELETED);
        when(deviceEventMock.getDeviceId()).thenReturn("TestId");

        UserToken userTokenMock1 = mock(UserToken.class);
        when(userTokenMock1.getToken()).thenReturn("1");

        UserToken userTokenMock2 = mock(UserToken.class);
        when(userTokenMock2.getToken()).thenReturn("2");

        when(userTokenRepositoryMock.findByDeviceId(deviceEventMock.getDeviceId())).thenReturn(Arrays.asList(userTokenMock1, userTokenMock2));

        deviceDeletedEventHandler.handle(deviceEventMock);

        verify(userTokenRepositoryMock).findByDeviceId(deviceEventMock.getDeviceId());

        verify(authorizationRulesRepositoryMock).deleteByToken("1");
        verify(userTokenRepositoryMock).delete("1");

        verify(authorizationRulesRepositoryMock).deleteByToken("2");
        verify(userTokenRepositoryMock).delete("2");

        verifyNoMoreInteractions(authorizationRulesRepositoryMock, userTokenRepositoryMock);
    }

    @Test
    public void testGetEventType() throws Exception {
        assertThat(deviceDeletedEventHandler.getEventType()).isEqualTo(DeviceEvent.class);
    }
}