package io.corbel.iam.service;

import java.util.Map;

import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;

/**
 * @author Alberto J. Rubio
 */
public interface EventsService {

    void sendUserCreatedEvent(User user);

    void sendUserModifiedEvent(User user);

    void sendUserAuthenticationEvent(User user);

    void sendUserDeletedEvent(User user, String domain);

    void sendNotificationEvent(String domainId, String notificationId, String recipient, Map<String, String> properties);

    void sendDomainDeletedEvent(String domainId);

    void sendCreateScope(String scope);

    void sendDeleteScope(String scope);

    void sendClientAuthenticationEvent(String domainId, String id);

    void sendDeviceCreateEvent(Device device);

    void sendDeviceUpdateEvent(Device device);

    void sendDeviceDeleteEvent(String deviceUid, String userId, String domainId);

    void sendUpdateDomainPublicScopesEvent(String domainId);
}
