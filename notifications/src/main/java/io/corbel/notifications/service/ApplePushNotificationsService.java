package io.corbel.notifications.service;

import io.corbel.notifications.model.NotificationTemplate;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Alberto J. Rubio
 */
public class ApplePushNotificationsService implements NotificationsService {

    private static final Logger LOG = LoggerFactory.getLogger(ApplePushNotificationsService.class);

    private final ApnsService apnsService;

    public ApplePushNotificationsService(ApnsService apnsService) {
        this.apnsService = apnsService;
    }

    @Override
    public void send(NotificationTemplate notificationTemplate, String... recipients) {
        try {
            String payload = APNS.newPayload().badge(1).alertTitle(notificationTemplate.getTitle())
                    .alertBody(notificationTemplate.getText()).build();
            apnsService.push(Arrays.asList(recipients), payload);
            LOG.info("Apple push notification sent to: " + Arrays.toString(recipients));
        } catch (Exception e) {
            LOG.error("Sending apple push notification error: {}", e.getMessage(), e);
        }
    }
}
