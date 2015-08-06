package io.corbel.iam.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.event.AuthorizationEvent;
import io.corbel.event.DomainDeletedEvent;
import io.corbel.event.NotificationEvent;
import io.corbel.event.ScopeUpdateEvent;
import io.corbel.event.UserCreatedEvent;
import io.corbel.event.UserDeletedEvent;
import io.corbel.eventbus.service.EventBus;
import io.corbel.iam.model.User;

/**
 * Created by Alberto J. Rubio
 */
public class DefaultEventsService implements EventsService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventsService.class);
    private final EventBus eventBus;

    public DefaultEventsService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void sendUserCreatedEvent(User user) {
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(user.getId(), user.getDomain(), user.getEmail(), user.getCountry());
        eventBus.dispatch(userCreatedEvent);
    }

    @Override
    public void sendUserDeletedEvent(String id, String domain) {
        UserDeletedEvent userDeletedEvent = new UserDeletedEvent(id, domain);
        eventBus.dispatch(userDeletedEvent);
    }

    @Override
    public void sendNotificationEvent(String notificationId, String recipient, Map<String, String> properties) {
        NotificationEvent notificationEvent = new NotificationEvent(notificationId, recipient);
        notificationEvent.setProperties(properties);
        eventBus.dispatch(notificationEvent);
        LOG.info("Sending email from IAM with notification: {}", notificationId);
    }

    @Override
    public void sendDomainDeletedEvent(String domainId) {
        eventBus.dispatch(new DomainDeletedEvent(domainId));
    }

    @Override
    public void sendCreateScope(String scope) {
        eventBus.dispatch(ScopeUpdateEvent.createScopeEvent(scope, null));
    }

    @Override
    public void sendDeleteScope(String scope) {
        eventBus.dispatch(ScopeUpdateEvent.deleteScopeEvent(scope, null));
    }

    @Override
    public void sendUserAuthenticationEvent(String domainId, String id) {
        eventBus.dispatch(AuthorizationEvent.userAuthenticationEvent(domainId, id));
    }

    @Override
    public void sendClientAuthenticationEvent(String domainId, String id) {
        eventBus.dispatch(AuthorizationEvent.clientAuthenticationEvent(domainId, id));
    }
}
