package com.bq.oss.corbel.iam.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bq.oss.corbel.event.AuthorizationEvent;
import com.bq.oss.corbel.event.DomainDeletedEvent;
import com.bq.oss.corbel.event.NotificationEvent;
import com.bq.oss.corbel.event.ScopeUpdateEvent;
import com.bq.oss.corbel.event.UserCreatedEvent;
import com.bq.oss.corbel.event.UserDeletedEvent;
import com.bq.oss.corbel.eventbus.service.EventBus;
import com.bq.oss.corbel.iam.model.User;

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
