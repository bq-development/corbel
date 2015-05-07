package com.bq.oss.corbel.iam.repository;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Alberto J. Rubio
 */
public interface UserRepository extends GenericFindRepository<User, String>, HasScopesRepository<String>, UserRepositoryCustom {

    User findById(String id);

    User findByUsernameAndDomain(String username, String domainId);

    User findByDomainAndEmail(String domain, String email);

}
