package io.corbel.oauth.service;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.oauth.mail.NotificationConfiguration;
import io.corbel.oauth.model.Client;

/**
 * @author Alberto J. Rubio
 */
public class DefaultMailResetPasswordService implements MailResetPasswordService {

    private final NotificationConfiguration mailConfiguration;
    private final SendNotificationService sendNotificationService;
    private final TokenFactory tokenFactory;

    public DefaultMailResetPasswordService(NotificationConfiguration notificationConfiguration,
            SendNotificationService sendNotificationService, TokenFactory tokenFactory) {
        this.sendNotificationService = sendNotificationService;
        this.mailConfiguration = notificationConfiguration;
        this.tokenFactory = tokenFactory;
    }

    @Override
    public void sendMailResetPassword(Client client, String userId, String email) {
        String resetUrl = Optional.ofNullable(client.getResetUrl()).orElse(mailConfiguration.getClientUrl());
        String notificationId = Optional.ofNullable(client.getResetNotificationId()).orElse(mailConfiguration.getNotificationId());
        String clientUrl = resetUrl.replace("{token}", createEmailResetPasswordToken(client.getName(), userId));
        sendNotificationService.sendNotification(client.getDomain(), notificationId, email, ImmutableMap.of("clientUrl", clientUrl));
    }

    private String createEmailResetPasswordToken(String clientId, String userId) {
        return tokenFactory.createToken(
                TokenInfo.newBuilder().setType(TokenType.TOKEN).setUserId(userId).setClientId(clientId).setOneUseToken(true).build(),
                mailConfiguration.getTokenDurationInSeconds()).getAccessToken();
    }
}
