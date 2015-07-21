package com.bq.oss.corbel.iam.repository;

import java.util.List;

import com.bq.oss.corbel.iam.model.Group;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

public interface GroupRepositoryCustom {

    List<Group> findByDomain(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort);

}
