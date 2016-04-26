package io.corbel.oauth.service;

import java.util.Map;

/**
 * @author Alberto J. Rubio
 */
public interface SendNotificationService {

    void sendNotification(String domain, String notificationsId, String recipient, Map<String, String> properties);
}
