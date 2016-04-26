package io.corbel.notifications.repository;

import io.corbel.lib.mongo.repository.PartialUpdateRepository;
import io.corbel.lib.queries.mongo.repository.GenericFindRepository;
import io.corbel.notifications.model.NotificationTemplate;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Francisco Sanchez
 */
public interface NotificationRepository extends CrudRepository<NotificationTemplate, String>, GenericFindRepository<NotificationTemplate, String> {

    NotificationTemplate findByDomainAndName(String domain, String name);

    Long deleteByDomainAndName(String domain, String name);
}
