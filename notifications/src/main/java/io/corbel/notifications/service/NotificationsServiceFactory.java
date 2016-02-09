package io.corbel.notifications.service;

/**
 * Created by Alberto J. Rubio
 */
public interface NotificationsServiceFactory {

    NotificationsService getNotificationService(String type);
}
