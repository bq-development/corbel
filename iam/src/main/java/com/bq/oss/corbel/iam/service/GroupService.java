package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import com.bq.oss.corbel.iam.exception.GroupAlreadyExistsException;
import com.bq.oss.corbel.iam.model.Group;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

public interface GroupService {

    Optional<Group> get(String id);

    Optional<Group> get(String id, String domain);

    List<Group> getAll(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort);

    Group create(Group group) throws GroupAlreadyExistsException;

    void addScopes(String id, String... scopes);

    void removeScopes(String id, String... scopes);

    void delete(String id, String domain);

}
