package io.corbel.oauth.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;

import io.corbel.event.NotificationEvent;
import io.corbel.eventbus.service.EventBus;

/**
 * Created by Francisco Sanchez
 */
public class DefaultSendNotificationServiceTest {
    String TEST_DOMAIN_ID = "domain_test_id";
    String NOTIFICATION_ID_TEST = "notification:id:test";
    String EMAIL_TEST = "me@silkroad";
    Map PROPERTIES_TEST = ImmutableMap.of("key1", "value1", "key2", "value2");
    SendNotificationService sendNotificationService;
    EventBus eventBus;

    @Before
    public void setup() {
        eventBus = mock(EventBus.class);
        sendNotificationService = new DefaultSendNotificationService(eventBus);
    }

    @Test
    public void testSendNotification() {
        sendNotificationService.sendNotification(TEST_DOMAIN_ID, NOTIFICATION_ID_TEST, EMAIL_TEST, PROPERTIES_TEST);

        ArgumentCaptor<NotificationEvent> notificationEventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventBus).dispatch(notificationEventCaptor.capture());

        assertThat(notificationEventCaptor.getValue().getDomain()).isEqualTo(TEST_DOMAIN_ID);
        assertThat(notificationEventCaptor.getValue().getNotificationId()).isEqualTo(NOTIFICATION_ID_TEST);
        assertThat(notificationEventCaptor.getValue().getRecipient()).isEqualTo(EMAIL_TEST);
        assertThat(notificationEventCaptor.getValue().getProperties()).isEqualTo(PROPERTIES_TEST);

    }
}
