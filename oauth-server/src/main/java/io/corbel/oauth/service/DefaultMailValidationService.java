package io.corbel.oauth.service;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.oauth.mail.EmailValidationConfiguration;
import io.corbel.oauth.model.Client;

/**
 * @author Alberto J. Rubio
 */
public class DefaultMailValidationService implements MailValidationService {

    private final EmailValidationConfiguration emailValidationConfiguration;
    private final SendNotificationService sendNotificationService;
    private final TokenFactory tokenFactory;
    private final ClientService clientService;

    public DefaultMailValidationService(EmailValidationConfiguration emailValidationConfiguration,
            SendNotificationService sendNotificationService, TokenFactory tokenFactory, ClientService clientService) {
        this.sendNotificationService = sendNotificationService;
        this.emailValidationConfiguration = emailValidationConfiguration;
        this.tokenFactory = tokenFactory;
        this.clientService = clientService;
    }

    @Override
    public void sendMailValidation(Client client, String userId, String email) {
        Boolean validatedEnabled = Optional.of(client.isValidationEnabled()).orElse(emailValidationConfiguration.isValidationEnabled());

        if (validatedEnabled) {
            String validationtUrl = Optional.ofNullable(client.getValidationUrl()).orElse(emailValidationConfiguration.getClientUrl());
            String notificationId = Optional.ofNullable(client.getValidationNotificationId())
                    .orElse(emailValidationConfiguration.getNotificationId());
            String clientUrl = validationtUrl.replace("{token}", createEmailValidationToken(client.getName(), userId, email));
            sendNotificationService.sendNotification(client.getDomain(), notificationId, email, ImmutableMap.of("clientUrl", clientUrl));
        }
    }

    private String createEmailValidationToken(String clientId, String userId, String email) {
        return tokenFactory.createToken(TokenInfo.newBuilder().setType(TokenType.TOKEN).setUserId(userId).setClientId(clientId)
                .setState(email).setOneUseToken(true).build(), emailValidationConfiguration.getTokenDurationInSeconds()).getAccessToken();
    }
}
