package com.bq.oss.corbel.iam.service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.repository.ClientRepository;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.factory.TokenFactory;
import com.bq.oss.lib.token.model.TokenType;

public class DefaultMailResetPasswordService implements MailResetPasswordService {
    private final EventsService eventsService;
    private final ScopeService scopeService;
    private final TokenFactory tokenFactory;
    private final ClientRepository clientRepository;
    private final String resetPasswordTokenScope;
    private final Clock clock;
    private final long defaultTokenDurationInSeconds;
    private final String defaultNotificationId;
    private final String defaultResetUrl;

    public DefaultMailResetPasswordService(EventsService eventsService, ScopeService scopeService, TokenFactory tokenFactory,
            ClientRepository clientRepository, String resetPasswordTokenScope, Clock clock, long tokenDurationInSeconds,
            String notificationId, String defaultResetUrl) {
        this.eventsService = eventsService;
        this.scopeService = scopeService;
        this.tokenFactory = tokenFactory;
        this.clientRepository = clientRepository;
        this.resetPasswordTokenScope = resetPasswordTokenScope;
        this.clock = clock;
        this.defaultTokenDurationInSeconds = tokenDurationInSeconds;
        this.defaultNotificationId = notificationId;
        this.defaultResetUrl = defaultResetUrl;

    }

    @Override
    public void sendMailResetPassword(String clientId, String userId, String email, String domainId) {
        Optional.ofNullable(clientRepository.findOne(clientId)).ifPresent(client -> {
            String notificationId = Optional.ofNullable(client.getResetNotificationId()).orElse(defaultNotificationId);
            sendMailResetPasswordEvent(notificationId, client, userId, email, domainId);
        });
    }

    private void sendMailResetPasswordEvent(String notificationId, Client client, String userId, String email, String domainId) {

        String token = createEmailResetPasswordToken(client.getId(), userId, domainId);
        setTokenScope(token, client.getId(), userId);

        String resetUrl = Optional.ofNullable(client.getResetUrl()).orElse(defaultResetUrl);
        String clientUrl = resetUrl.replace("{token}", token);

        Map<String, String> properties = new HashMap<>();
        properties.put("clientUrl", clientUrl);

        eventsService.sendNotificationEvent(notificationId, email, properties);
    }

    private String createEmailResetPasswordToken(String clientId, String userId, String domainId) {
        return tokenFactory.createToken(
                TokenInfo.newBuilder().setType(TokenType.TOKEN).setOneUseToken(true).setUserId(userId).setClientId(clientId)
                        .setDomainId(domainId).build(), defaultTokenDurationInSeconds).getAccessToken();
    }

    private void setTokenScope(String token, String clientId, String userId) {
        long expireAt = clock.instant().plus(defaultTokenDurationInSeconds, ChronoUnit.SECONDS).toEpochMilli();
        Set<String> scopes = new HashSet<>();
        scopes.add(resetPasswordTokenScope);
        scopeService.publishAuthorizationRules(token, expireAt, scopes, userId, clientId);
    }
}