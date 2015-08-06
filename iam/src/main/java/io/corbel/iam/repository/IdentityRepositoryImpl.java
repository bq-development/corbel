package io.corbel.iam.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import io.corbel.iam.model.Identity;
import io.corbel.lib.mongo.utils.MongoCommonOperations;
import com.google.common.collect.ImmutableMap;

/**
 * @author Rubén Carrasco
 *
 */
public class IdentityRepositoryImpl implements IdentityRepositoryCustom {

    private final MongoOperations mongo;

    @Autowired
    public IdentityRepositoryImpl(MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public boolean existsByDomainAndUserIdAndOauthService(String domain, String userId, String oauthService) {
        return MongoCommonOperations.exists(mongo, ImmutableMap.of("domain", domain, "userId", userId, "oauthService", oauthService),
                Identity.class);
    }

    @Override
    public void deleteByUserIdAndDomain(String userId, String domain) {
        MongoCommonOperations.delete(mongo, ImmutableMap.of("domain", domain, "userId", userId), Identity.class);
    }

}
