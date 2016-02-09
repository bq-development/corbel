package io.corbel.notifications.service;

import io.corbel.notifications.model.NotificationTemplate;

/**
 * Created by Alberto J. Rubio
 */
public interface NotificationsService {

    void send(NotificationTemplate notificationTemplate, String ... recipient);
}
