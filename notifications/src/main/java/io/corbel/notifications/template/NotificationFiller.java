package io.corbel.notifications.template;

import io.corbel.notifications.model.NotificationTemplate;

import java.util.Map;

/**
 * @author Francisco Sanchez
 */
public interface NotificationFiller {

	NotificationTemplate fill(NotificationTemplate notificationTemplate, Map<String, String> properties);
}
