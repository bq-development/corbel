package io.corbel.notifications.api;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;

/**
 * @author Alexander De Leon
 * 
 */
public final class NotificationsErrorResponseFactory extends ErrorResponseFactory {

	private static NotificationsErrorResponseFactory INSTANCE;

	public static NotificationsErrorResponseFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NotificationsErrorResponseFactory();
		}
		return INSTANCE;
	}

	private NotificationsErrorResponseFactory() { }

}
