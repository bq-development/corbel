package io.corbel.notifications.model;

import io.corbel.lib.mongo.IdGenerator;
import io.corbel.notifications.utils.DomainNameIdGenerator;

public class NotificationTemplateIdGenerator implements IdGenerator<NotificationTemplate> {

    @Override
    public String generateId(NotificationTemplate notificationTemplate) {
        return DomainNameIdGenerator.generateNotificationTemplateId(notificationTemplate.getDomain(), notificationTemplate.getName());
    }
}
