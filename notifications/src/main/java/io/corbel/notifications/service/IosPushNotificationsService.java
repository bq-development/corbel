package io.corbel.notifications.service;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Alberto J. Rubio
 */
public class IosPushNotificationsService implements NotificationsService {

    private static final Logger LOG = LoggerFactory.getLogger(IosPushNotificationsService.class);

    private final Map<String, ApnsService> apnsServices = new HashMap<>();

    @Override
    public void send(Domain domain, NotificationTemplate notificationTemplate, String... recipients) {
        try {
            if (!apnsServices.containsKey(domain.getId())) {
                createApnsService(domain);
            }
            String payload = APNS.newPayload().badge(1).alertTitle(notificationTemplate.getTitle())
                    .alertBody(notificationTemplate.getText()).build();
            apnsServices.get(domain.getId()).push(Arrays.asList(recipients), payload);
            LOG.info("Ios push notification sent to: " + Arrays.toString(recipients));
        } catch (Exception e) {
            LOG.error("Sending Ios push notification error: {}", e.getMessage(), e);
        }
    }

    private void createApnsService(Domain domain) {
        byte[] certificate = domain.getIosNotificationsCertificate().getBytes(StandardCharsets.UTF_8);
        ApnsServiceBuilder apnsServiceBuilder = APNS.newService()
                .withCert(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)),
                domain.getIosNotificationsPassword());
        apnsServiceBuilder.withProductionDestination();
        apnsServices.put(domain.getId(), apnsServiceBuilder.build());
    }
}
