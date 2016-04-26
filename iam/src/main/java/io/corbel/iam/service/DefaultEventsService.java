package io.corbel.iam.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.event.*;
import io.corbel.eventbus.service.EventBus;
import io.corbel.iam.model.Device;
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
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(user.getDomain(), user.getId(), user.getEmail(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getProfileUrl(), user.getPhoneNumber(), user.getCountry(),
                user.getProperties(), user.getScopes(), user.getGroups());
        eventBus.dispatch(userCreatedEvent);
    }

    @Override
    public void sendUserDeletedEvent(User user, String domain) {
        UserDeletedEvent userDeletedEvent = new UserDeletedEvent(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), domain);
        eventBus.dispatch(userDeletedEvent);
    }

    @Override
    public void sendUserModifiedEvent(User user) {
        UserModifiedEvent event = new UserModifiedEvent(user.getDomain(), user.getId(), user.getEmail(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getProfileUrl(), user.getPhoneNumber(), user.getCountry(),
                user.getProperties(), user.getScopes(), user.getGroups());
        eventBus.dispatch(event);
    }

    @Override
    public void sendUserAuthenticationEvent(User user) {
        UserAuthenticationEvent event = new UserAuthenticationEvent(user.getDomain(), user.getId(), user.getEmail(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getProfileUrl(), user.getPhoneNumber(), user.getCountry(),
                user.getProperties(), user.getScopes(), user.getGroups());
        eventBus.dispatch(event);
    }

    @Override
    public void sendNotificationEvent(String domainId, String notificationId, String recipient, Map<String, String> properties) {
        NotificationEvent notificationEvent = new NotificationEvent(notificationId, recipient, domainId);
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
    public void sendClientAuthenticationEvent(String domainId, String id) {
        eventBus.dispatch(AuthorizationEvent.clientAuthenticationEvent(domainId, id));
    }

    @Override
    public void sendDeviceCreateEvent(Device device) {
        eventBus.dispatch(new DeviceEvent(DeviceEvent.Type.CREATED, device.getDomain(), device.getUid(), device.getUserId(),
                device
.getType(), device.getName()));
    }

    @Override
    public void sendDeviceUpdateEvent(Device device) {
        eventBus.dispatch(new DeviceEvent(DeviceEvent.Type.UPDATED, device.getDomain(), device.getUid(), device.getUserId(),
                device
.getType(), device.getName()));

    }

    @Override
    public void sendDeviceDeleteEvent(String deviceUid, String userId, String domainId) {
        eventBus.dispatch(new DeviceEvent(DeviceEvent.Type.DELETED, domainId, deviceUid, userId));
    }

    @Override
    public void sendUpdateDomainPublicScopesEvent(String domainId) {
        eventBus.dispatch(new DomainPublicScopesNotPublishedEvent(domainId));
    }

}
