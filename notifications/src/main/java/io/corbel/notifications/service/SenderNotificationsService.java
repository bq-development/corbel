package io.corbel.notifications.service;

import java.util.Map;

/**
 * @author Cristian del Cerro
 */
public interface SenderNotificationsService {

    void sendNotification(String domainId, String notificationId, Map<String, String> customProperties, String ... recipients);
}
