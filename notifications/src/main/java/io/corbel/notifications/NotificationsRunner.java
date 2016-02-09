package io.corbel.notifications;

import io.corbel.lib.ws.cli.GenericConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.corbel.notifications.ioc.NotificationsIoc;
import io.corbel.notifications.ioc.NotificationsListenerIoc;

/**
 * Created by Alberto J. Rubio
 */
public class NotificationsRunner {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationsRunner.class);

	public static void main(String[] args) {
		try {
			System.setProperty("conf.namespace", "notifications");

			ApplicationContext context = new AnnotationConfigApplicationContext(NotificationsListenerIoc.class);

			boolean restEnabled = context.getEnvironment().getProperty("notifications.rest.enabled", Boolean.class);

			if (restEnabled) {
				LOG.info("Starting Notifications REST api.");
				NotificationsService notificationsService = new NotificationsService(context);
				notificationsService.setCommandLine(new GenericConsole(notificationsService.getArtifactId(), NotificationsIoc.class));
				notificationsService.run(args);
			}
		} catch (Exception e) {
			LOG.error("Unable to start Notifications", e);
		}
	}
}
