package io.corbel.notifications.service;

import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;

/**
 * Created by Alberto J. Rubio
 */
public class NotificationsDispatcher {

    private final NotificationsServiceFactory notificationsServiceFactory;

    public NotificationsDispatcher(NotificationsServiceFactory notificationsServiceFactory) {
        this.notificationsServiceFactory = notificationsServiceFactory;
    }

    public void send(Domain domain, NotificationTemplate notificationTemplate, String ... recipients) {
        NotificationsService notificationsService =
                notificationsServiceFactory.getNotificationService(notificationTemplate.getType());
        notificationsService.send(domain, notificationTemplate, recipients);
    }
}
