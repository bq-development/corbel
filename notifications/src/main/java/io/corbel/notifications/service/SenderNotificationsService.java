package io.corbel.notifications.service;

import java.util.Map;

/**
 * @author Cristian del Cerro
 */
public interface SenderNotificationsService {

    void sendNotification(String notificationId, Map<String, String> properties, String recipient);
}
