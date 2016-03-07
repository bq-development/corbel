package io.corbel.oauth.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.event.NotificationEvent;
import io.corbel.eventbus.service.EventBus;

/**
 * @author Alberto J. Rubio
 */
public class DefaultSendNotificationService implements SendNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSendNotificationService.class);

    private final EventBus eventBus;

    public DefaultSendNotificationService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void sendNotification(String domain, String notificationsId, String recipient, Map<String, String> properties) {
        NotificationEvent notificationEvent = new NotificationEvent(notificationsId, recipient, domain);
        notificationEvent.setProperties(properties);
        eventBus.dispatch(notificationEvent);
        LOG.info("Sending email with notification: {}", notificationsId);
    }
}
