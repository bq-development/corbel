package io.corbel.iam.repository;

import io.corbel.iam.model.Group;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

import java.util.List;

public interface GroupRepositoryCustom {

    List<Group> findByDomain(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort);

    void insert(Group group);
}
