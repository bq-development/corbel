package io.corbel.oauth.repository;

import com.google.common.collect.ImmutableMap;
import io.corbel.lib.mongo.utils.MongoCommonOperations;
import io.corbel.oauth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * @author Ricardo Martínez
 */
public class UserRepositoryImpl implements UserRepositoryCustom{

    protected final MongoOperations mongo;
    private final Class entityClass = User.class;
    private static final String FIELD_DOMAIN = "domain";
    private static final String FIELD_USERNAME = "username";

    @Autowired
    public UserRepositoryImpl(MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public boolean existsByUsernameAndDomain(String username, String domainId) {
        return MongoCommonOperations.exists(mongo,
                ImmutableMap.of(FIELD_USERNAME, username, FIELD_DOMAIN, domainId), entityClass);
    }
}
