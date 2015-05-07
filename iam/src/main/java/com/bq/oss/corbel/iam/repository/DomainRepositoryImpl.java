package com.bq.oss.corbel.iam.repository;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.bq.oss.corbel.iam.model.Domain;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author Alexander De Leon
 * 
 */
@SuppressWarnings("unused") public class DomainRepositoryImpl extends HasScopesRepositoryBase<Domain, String> implements
        DomainRepositoryCustom {

    private final MongoOperations mongo;

    @Autowired
    public DomainRepositoryImpl(MongoOperations mongo) {
        super(mongo, Domain.class);
        this.mongo = mongo;
    }

    @Override
    public void addDefaultScopes(String id, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            return;
        }
        Query query = Query.query(Criteria.where(FIELD_ID).is(id));
        Update update = new Update();
        BasicDBList list = new BasicDBList();
        list.addAll(Arrays.asList(scopes));
        update.addToSet(DomainRepository.FIELD_DEFAULT_SCOPES, new BasicDBObject("$each", list));
        mongo.updateFirst(query, update, Domain.class);
    }

    @Override
    public void removeDefaultScopes(String id, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            return;
        }
        Query query = Query.query(Criteria.where(FIELD_ID).is(id));
        Update update = new Update();
        update.pullAll(DomainRepository.FIELD_DEFAULT_SCOPES, scopes);
        mongo.updateFirst(query, update, Domain.class);
    }

    @Override
    public void insert(Domain domain) {
        mongo.insert(domain);
    }

}
