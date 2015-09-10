package io.corbel.iam.repository;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author Alexander De Leon
 * 
 */
public class HasScopesRepositoryBase<ENTITY, ID> implements HasScopesRepository<ID> {

    protected final MongoOperations mongo;
    private final Class<ENTITY> entityClass;

    @Autowired
    public HasScopesRepositoryBase(MongoOperations mongo, Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
        this.mongo = mongo;
    }

    @Override
    public void addScopes(ID id, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            return;
        }
        Criteria criteria = Criteria.where(FIELD_ID).is(id);
        Query query = Query.query(criteria);
        Update update = new Update();
        BasicDBList list = new BasicDBList();
        list.addAll(Arrays.asList(scopes));
        update.addToSet(FIELD_SCOPES, new BasicDBObject("$each", list));
        mongo.updateFirst(query, update, entityClass);
    }

    @Override
    public void removeScopes(ID id, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            return;
        }
        Criteria criteria = Criteria.where(FIELD_ID).is(id);
        Query query = Query.query(criteria);
        Update update = new Update();
        update.pullAll(FIELD_SCOPES, scopes);
        mongo.updateFirst(query, update, entityClass);
    }

    @Override
    public void removeScopes(String... scopes){
        List<String> scopesIds = Arrays.asList(scopes);
        Criteria criteria = Criteria.where(FIELD_SCOPES).in(scopesIds);
        Query query = Query.query(criteria);
        Update update = new Update();
        update.pullAll(FIELD_SCOPES, scopes);
        mongo.updateMulti(query, update, entityClass);
    }

    protected MongoOperations getMongo() {
        return mongo;
    }

}
