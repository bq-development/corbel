package io.corbel.notifications.eventbus;

import io.corbel.event.NotificationEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.notifications.service.SenderNotificationsService;

/**
 * Created by Alberto J. Rubio
 */
public class NotificationEventHandler implements EventHandler<NotificationEvent> {

	private SenderNotificationsService senderNotificationsService;

	public NotificationEventHandler(SenderNotificationsService senderNotificationsService) {
		this.senderNotificationsService = senderNotificationsService;
	}

	@Override
	public void handle(NotificationEvent event) {
		senderNotificationsService.sendNotification(event.getDomain(), event.getNotificationId(), event.getProperties(),
				event.getRecipient());
	}

	@Override
	public Class<NotificationEvent> getEventType() {
		return NotificationEvent.class;
	}
}
