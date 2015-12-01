package io.corbel.iam.service;

import io.corbel.event.AuthorizationEvent;
import io.corbel.event.DeviceEvent;
import io.corbel.event.DomainDeletedEvent;
import io.corbel.event.NotificationEvent;
import io.corbel.event.ScopeUpdateEvent;
import io.corbel.event.UserAuthenticationEvent;
import io.corbel.event.UserCreatedEvent;
import io.corbel.event.UserDeletedEvent;
import io.corbel.event.UserModifiedEvent;
import io.corbel.eventbus.service.EventBus;
import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void sendClientAuthenticationEvent(String domainId, String id) {
        eventBus.dispatch(AuthorizationEvent.clientAuthenticationEvent(domainId, id));
    }

    @Override
    public void sendDeviceCreateEvent(Device device) {
        eventBus.dispatch(new DeviceEvent(DeviceEvent.Type.CREATED, device.getDomain(), device.getId(), device.getUserId(), device
                .getType().name(), device.getName()));
    }

    @Override
    public void sendDeviceUpdateEvent(Device device) {
        eventBus.dispatch(new DeviceEvent(DeviceEvent.Type.UPDATED, device.getDomain(), device.getId(), device.getUserId(), device
                .getType().name(), device.getName()));

    }

    @Override
    public void sendDeviceDeleteEvent(String deviceId, String userId, String domainId) {
        eventBus.dispatch(new DeviceEvent(DeviceEvent.Type.DELETED, domainId, deviceId, userId));
    }

}
