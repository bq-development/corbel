package io.corbel.iam.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import io.corbel.iam.model.Group;

import io.corbel.lib.queries.mongo.builder.MongoQueryBuilder;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

public class GroupRepositoryImpl extends HasScopesRepositoryBase<Group, String>implements GroupRepositoryCustom {

    @Autowired
    public GroupRepositoryImpl(MongoOperations mongoOperations) {
        super(mongoOperations, Group.class);
    }

    @Override
    public List<Group> findByDomain(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort) {
        Query query = new MongoQueryBuilder().query(resourceQueries).pagination(pagination).sort(sort).build()
                .addCriteria(where("domain").is(domain));
        return mongo.find(query, Group.class);
    }

}
