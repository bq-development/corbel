package io.corbel.oauth.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.oauth.model.Client;

/**
 * Created by Francisco Sanchez
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultMailChangePasswordServiceTest {
    String TEST_DOMAIN_ID = "test_domain_id";
    String NOTIFICATION_ID_TEST = "notification:id:test";
    String CLIENT_ID_TEST = "TEST_CLIENT:id:test";
    String EMAIL_TEST = "me@silkroad";
    String USERNAME_TEST = "me";

    SendNotificationService sendNotificationService;
    private DefaultMailChangePasswordService mailChangePasswordService;

    @Mock
    Client TEST_CLIENT;

    @Before
    public void setup() {
        when(TEST_CLIENT.getDomain()).thenReturn(TEST_DOMAIN_ID);
        sendNotificationService = mock(SendNotificationService.class);
        mailChangePasswordService = new DefaultMailChangePasswordService(NOTIFICATION_ID_TEST, sendNotificationService);
    }

    @Test
    public void testSendMailChangePassword() {
        mailChangePasswordService.sendMailChangePassword(TEST_CLIENT, USERNAME_TEST, EMAIL_TEST);
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendNotificationService).sendNotification(eq(TEST_DOMAIN_ID), eq(NOTIFICATION_ID_TEST), eq(EMAIL_TEST), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("username")).isEqualTo(USERNAME_TEST);
    }

    @Test
    public void testSendMailChangePasswordWithOverwriteNotificationByClient() {
        when(TEST_CLIENT.getChangePasswordNotificationId()).thenReturn(CLIENT_ID_TEST);
        mailChangePasswordService.sendMailChangePassword(TEST_CLIENT, USERNAME_TEST, EMAIL_TEST);
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendNotificationService).sendNotification(eq(TEST_DOMAIN_ID), eq(CLIENT_ID_TEST), eq(EMAIL_TEST), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("username")).isEqualTo(USERNAME_TEST);
    }

}
