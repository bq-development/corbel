package io.corbel.oauth.service;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import io.corbel.oauth.model.Client;

/**
 * @author Francisco Sanchez
 */
public class DefaultMailChangePasswordService implements MailChangePasswordService {

    private final String notificationId;
    private final SendNotificationService sendNotificationService;

    public DefaultMailChangePasswordService(String notificationId, SendNotificationService sendNotificationService) {
        this.notificationId = notificationId;
        this.sendNotificationService = sendNotificationService;
    }

    @Override
    public void sendMailChangePassword(Client client, String username, String email) {
        String changePasswordNotificationId = Optional.ofNullable(client.getChangePasswordNotificationId()).orElse(notificationId);
        sendNotificationService.sendNotification(client.getDomain(), changePasswordNotificationId, email,
                ImmutableMap.of("username", username));
    }
}
