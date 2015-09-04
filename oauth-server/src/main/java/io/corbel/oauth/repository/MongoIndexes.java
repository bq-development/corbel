package io.corbel.oauth.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;

import io.corbel.lib.mongo.index.MongoIndex;
import io.corbel.oauth.model.User;

/**
 * @author Rubén Carrasco
 */
public class MongoIndexes {

    private static final Logger LOG = LoggerFactory.getLogger(MongoIndexes.class);

    @Autowired
    public void ensureIndexes(MongoOperations mongo) {
        LOG.info("Creating mongo indexes");
        mongo.indexOps(User.class).ensureIndex(
                new MongoIndex().on("username", Direction.ASC).on("domain", Direction.ASC).unique().background().getIndexDefinition());

        mongo.indexOps(User.class).ensureIndex(
                new MongoIndex().on("email", Direction.ASC).on("domain", Direction.ASC).unique().background().getIndexDefinition());
    }
}
