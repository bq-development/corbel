package io.corbel.notifications.repository;

import io.corbel.lib.queries.mongo.repository.GenericFindRepository;
import io.corbel.notifications.model.Domain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DomainRepository extends CrudRepository<Domain, String>,
        GenericFindRepository<Domain, String> {

}
