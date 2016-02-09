package io.corbel.notifications.service;

import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.template.NotificationFiller;

import java.util.Map;

/**
 * @author Cristian del Cerro
 */
public class DefaultSenderNotificationsService implements SenderNotificationsService {

    NotificationFiller notificationFiller;
    NotificationsDispatcher notificationsDispatcher;
    NotificationRepository notificationRepository;

    public DefaultSenderNotificationsService(NotificationFiller notificationFiller, NotificationsDispatcher notificationsDispatcher, NotificationRepository notificationRepository) {
        this.notificationFiller = notificationFiller;
        this.notificationsDispatcher = notificationsDispatcher;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void sendNotification(String notificationId, Map<String, String> properties, String recipient) {
        NotificationTemplate notificationTemplate = notificationRepository.findOne(notificationId);
        if(notificationTemplate != null) {
            NotificationTemplate notificationTemplateFilled = notificationFiller.fill(notificationTemplate, properties);
            notificationsDispatcher.send(notificationTemplateFilled, recipient);
        }
    }
}
