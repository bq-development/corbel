package io.corbel.notifications.service;

import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.repository.DomainRepository;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.template.NotificationFiller;
import io.corbel.notifications.utils.DomainNameIdGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Cristian del Cerro
 */
public class DefaultSenderNotificationsService implements SenderNotificationsService {

    NotificationFiller notificationFiller;
    NotificationsDispatcher notificationsDispatcher;
    NotificationRepository notificationRepository;
    DomainRepository domainRepository;

    public DefaultSenderNotificationsService(NotificationFiller notificationFiller,
                                             NotificationsDispatcher notificationsDispatcher,
                                             NotificationRepository notificationRepository,
                                             DomainRepository domainRepository) {
        this.notificationFiller = notificationFiller;
        this.notificationsDispatcher = notificationsDispatcher;
        this.notificationRepository = notificationRepository;
        this.domainRepository = domainRepository;
    }

    @Override
    public void sendNotification(String domainId, String notificationId, Map<String, String> customProperties, String recipient) {
        String currentNotificationId = DomainNameIdGenerator.generateNotificationTemplateId(domainId, notificationId);

        Domain domain = domainRepository.findOne(domainId);

        String notificationTemplateId = Optional.ofNullable(domain)
                .map(currentDomain -> currentDomain.getTemplates())
                .map(currentTemplate -> currentTemplate.get(currentNotificationId))
                .orElse(currentNotificationId);

        Map<String, String> properties = Optional.ofNullable(domain)
                .map(currentDomain -> getProperties(currentDomain, customProperties))
                .orElse(customProperties);


        NotificationTemplate notificationTemplate = notificationRepository.findOne(notificationTemplateId);
        if (notificationTemplate != null) {
            NotificationTemplate notificationTemplateFilled = notificationFiller.fill(notificationTemplate, properties);
            notificationsDispatcher.send(notificationTemplateFilled, recipient);
        }

    }

    private Map<String, String> getProperties(Domain domain, Map<String, String> customProperties) {
        Map<String, String> propertiesByDomain = domain.getProperties() != null ? domain.getProperties() : Collections.emptyMap();

        Map<String, String> properties = new HashMap<>();
        properties.putAll(propertiesByDomain);

        if(customProperties != null) {
            properties.putAll(customProperties);
        }

        return  properties;
    }


}
