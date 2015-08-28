package io.corbel.iam.repository;

import io.corbel.iam.model.Group;
import io.corbel.lib.queries.mongo.builder.MongoQueryBuilder;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class GroupRepositoryImpl extends HasScopesRepositoryBase<Group, String> implements GroupRepositoryCustom {

    private static final String FIELD_DOMAIN = "domain";
    private static final String FIELD_NAME = "name";

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

    @Override
    public void insert(Group group) {
        mongo.insert(group);
    }
}
