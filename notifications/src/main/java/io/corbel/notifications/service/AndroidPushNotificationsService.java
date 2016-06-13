package io.corbel.notifications.service;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Alberto J. Rubio
 */
public class AndroidPushNotificationsService implements NotificationsService {

    private final int PUSH_NOTIFICATIONS_RETRIES = 5;

    private static final Logger LOG = LoggerFactory.getLogger(AndroidPushNotificationsService.class);

    @Override
    public void send(Domain domain, NotificationTemplate notificationTemplate, String... recipients) {
        Sender sender = new Sender(notificationTemplate.getSender());
        Message msg = new Message.Builder().addData("message", notificationTemplate.getText()).build();
        try {
            sender.send(msg, Arrays.asList(recipients), PUSH_NOTIFICATIONS_RETRIES);
            LOG.info("Android push notification sent to: " + Arrays.toString(recipients));
        } catch (IOException e) {
            LOG.error("Sending android push notification error: {}", e.getMessage(), e);
        }
    }
}
