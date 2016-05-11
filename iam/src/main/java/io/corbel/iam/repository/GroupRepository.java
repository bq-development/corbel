package io.corbel.iam.repository;

import io.corbel.iam.model.Group;

import io.corbel.lib.mongo.repository.PartialUpdateRepository;
import io.corbel.lib.queries.mongo.repository.GenericFindRepository;

public interface GroupRepository extends PartialUpdateRepository<Group, String>, GenericFindRepository<Group, String>,
        HasScopesRepository<String>, GroupRepositoryCustom {

    Group findByIdAndDomain(String id, String domain);

    Group findByNameAndDomain(String name, String domain);

    Long deleteByIdAndDomain(String id, String domain);

    void deleteScopes(String... scopesId);

}
