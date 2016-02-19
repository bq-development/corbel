package io.corbel.notifications.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.corbel.lib.ws.dw.ioc.RabbitMQHealthCheckIoc;
import io.corbel.event.NotificationEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.eventbus.ioc.EventBusListeningIoc;
import io.corbel.notifications.eventbus.NotificationEventHandler;
import io.corbel.notifications.service.SenderNotificationsService;

/**
 * Created by Alberto J. Rubio
 */
@Configuration
@Import({ NotificationsIoc.class, EventBusListeningIoc.class, RabbitMQHealthCheckIoc.class })
public class NotificationsListenerIoc {

	@Bean
	public EventHandler<NotificationEvent> getMailEventHandler(SenderNotificationsService senderNotificationsService) {
		return new NotificationEventHandler(senderNotificationsService);
	}

}
