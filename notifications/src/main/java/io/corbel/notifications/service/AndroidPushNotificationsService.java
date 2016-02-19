package io.corbel.notifications.service;

import io.corbel.notifications.model.NotificationTemplate;
import com.phonedeck.gcm4j.DefaultGcm;
import com.phonedeck.gcm4j.Gcm;
import com.phonedeck.gcm4j.GcmConfig;
import com.phonedeck.gcm4j.GcmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Alberto J. Rubio
 */
public class AndroidPushNotificationsService implements NotificationsService {

	private static final Logger LOG = LoggerFactory.getLogger(AndroidPushNotificationsService.class);

	@Override
	public void send(NotificationTemplate notificationTemplate, String... recipients) {
		try {
			Gcm gcm = new DefaultGcm(new GcmConfig().withKey(notificationTemplate.getSender()));
			GcmRequest request = new GcmRequest().withRegistrationIds(Arrays.asList(recipients))
					.withCollapseKey(notificationTemplate.getTitle()).withDelayWhileIdle(true)
					.withDataItem("text", notificationTemplate.getText());
			gcm.send(request);
			LOG.info("Android push notification sent to: " + Arrays.toString(recipients));
		} catch (Exception e) {
			LOG.error("Sending android push notification error: {}", e.getMessage(), e);
		}
	}
}
