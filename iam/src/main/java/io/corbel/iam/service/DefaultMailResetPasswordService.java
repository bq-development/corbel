package io.corbel.iam.service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.*;

import io.corbel.iam.model.Client;
import io.corbel.iam.model.Scope;
import io.corbel.iam.repository.ClientRepository;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;

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
        setTokenScope(token, client.getId(), userId, domainId);

        String resetUrl = Optional.ofNullable(client.getResetUrl()).orElse(defaultResetUrl);
        String clientUrl = resetUrl.replace("{token}", token);

        Map<String, String> properties = new HashMap<>();
        properties.put("clientUrl", clientUrl);

        eventsService.sendNotificationEvent(domainId, notificationId, email, properties);
    }

    private String createEmailResetPasswordToken(String clientId, String userId, String domainId) {
        return tokenFactory.createToken(
                TokenInfo.newBuilder().setType(TokenType.TOKEN).setOneUseToken(true).setUserId(userId).setClientId(clientId)
                        .setDomainId(domainId).build(), defaultTokenDurationInSeconds).getAccessToken();
    }

    private void setTokenScope(String token, String clientId, String userId, String domainId) {
        long expireAt = clock.instant().plus(defaultTokenDurationInSeconds, ChronoUnit.SECONDS).toEpochMilli();
        Set<String> scopes = new HashSet<>();
        scopes.add(resetPasswordTokenScope);
        Set<Scope> filledScopes = scopeService.fillScopes(scopeService.expandScopes(scopes), userId, clientId, domainId);
        scopeService.publishAuthorizationRules(token, expireAt, filledScopes);
    }
}