package io.corbel.iam.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.exception.NotExistentScopeException;
import io.corbel.iam.model.Group;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

public interface GroupService {

    Optional<Group> getById(String id);

    Optional<Group> getById(String id, String domain);

    List<Group> getAll(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort);

    Set<String> getGroupScopes(String domain, Collection<String> groups);

    Group create(Group group) throws GroupAlreadyExistsException, NotExistentScopeException;

    void addScopes(String id, String... scopes) throws NotExistentScopeException;

    void removeScopes(String id, String... scopes);

    void delete(String id, String domain);

}
