package io.corbel.iam.repository;

import io.corbel.iam.model.User;
import io.corbel.lib.mongo.utils.MongoCommonOperations;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Alexander De Leon
 * 
 */
public class UserRepositoryImpl extends HasScopesRepositoryBase<User, String> implements UserRepositoryCustom {

    private static final String FIELD_DOMAIN = "domain";
    private static final String COLLECTION = "user";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_EMAIL = "email";

    @Autowired
    public UserRepositoryImpl(MongoOperations mongo) {
        super(mongo, User.class);
    }

    @Override
    public String findUserDomain(String id) {
        return MongoCommonOperations.findStringFieldById(getMongo(), FIELD_DOMAIN, id, COLLECTION);
    }

    @Override
    public boolean existsByUsernameAndDomain(String username, String domainId) {
        return MongoCommonOperations.exists(mongo, ImmutableMap.of(FIELD_USERNAME, username, FIELD_DOMAIN, domainId), User.class);
    }

    @Override
    public boolean existsByEmailAndDomain(String email, String domainId) {
        return MongoCommonOperations.exists(mongo, ImmutableMap.of(FIELD_EMAIL, email, FIELD_DOMAIN, domainId), User.class);
    }

    @Override
    public void deleteByDomain(String domainId) {
        mongo.remove(query(where(FIELD_DOMAIN).is(domainId)), User.class);
    }
}
