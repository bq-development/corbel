package io.corbel.oauth.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.oauth.model.Client;

/**
 * Created by Francisco Sanchez
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultMailChangePasswordServiceTest {

    private static final String CLIENT_TEST = "test_client";
    String NOTIFICATION_ID_TEST = "notification:id:test";
    String CLIENT_ID_TEST = "TEST_CLIENT:id:test";
    String EMAIL_TEST = "me@silkroad";
    String USERNAME_TEST = "me";

    SendNotificationService sendNotificationService;
    private DefaultMailChangePasswordService mailChangePasswordService;

    Client TEST_CLIENT;

    @Before
    public void setup() {
        TEST_CLIENT = new Client();
        sendNotificationService = mock(SendNotificationService.class);
        mailChangePasswordService = new DefaultMailChangePasswordService(NOTIFICATION_ID_TEST, sendNotificationService);
    }

    @Test
    public void testSendMailChangePassword() {
        mailChangePasswordService.sendMailChangePassword(TEST_CLIENT, USERNAME_TEST, EMAIL_TEST);
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendNotificationService).sendNotification(eq(NOTIFICATION_ID_TEST), eq(EMAIL_TEST), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("username")).isEqualTo(USERNAME_TEST);
    }

    @Test
    public void testSendMailChangePasswordWithOverwriteNotificationByClient() {
        TEST_CLIENT.setChangePasswordNotificationId(CLIENT_ID_TEST);
        mailChangePasswordService.sendMailChangePassword(TEST_CLIENT, USERNAME_TEST, EMAIL_TEST);
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(sendNotificationService).sendNotification(eq(CLIENT_ID_TEST), eq(EMAIL_TEST), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("username")).isEqualTo(USERNAME_TEST);
    }

}
