package io.corbel.notifications.service;

import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;

/**
 * Created by Alberto J. Rubio
 */
public interface NotificationsService {

    void send(Domain domain, NotificationTemplate notificationTemplate, String ... recipient);
}
