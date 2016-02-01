package io.corbel.oauth.repository;

import io.corbel.lib.mongo.repository.PartialUpdateRepository;
import io.corbel.oauth.model.User;

/**
 * @author Alberto J. Rubio
 */
public interface UserRepository extends PartialUpdateRepository<User, String>, UserRepositoryCustom {

    User findByUsername(String username);

    User findByEmail(String email);

    User findById(String id);

    User findByEmailAndDomain(String email, String domain);

    User findByUsernameAndDomain(String username, String domain);

}
