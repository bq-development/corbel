package io.corbel.notifications.cli.dsl

import io.corbel.lib.cli.console.Description
import io.corbel.lib.cli.console.Shell
import io.corbel.notifications.model.NotificationTemplate
import io.corbel.notifications.repository.NotificationRepository

/**
 * @author Alberto J. Rubio
 *
 */
@Shell("notifications")
class NotificationsShell {

    NotificationRepository notificationRepository;

    public NotificationsShell(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository
    }

    @Description("Creates a new notification on the DB. The input parameter is a map containing the notification data.")
    def createNotification(notificationFields) {
        assert notificationFields.id : 'Notification id is required'
        assert notificationFields.sender : 'Notification sender is required'
        assert notificationFields.type : 'Notification type is required'
        assert notificationFields.text : 'Notification text is required'
        NotificationTemplate notification = new NotificationTemplate()
        notification.id = notificationFields.id
        notification.sender = notificationFields.sender
        notification.type = notificationFields.type
        notification.text = notificationFields.text
        notification.title = notificationFields.title
        notificationRepository.save(notification)
    }

}

