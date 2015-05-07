package com.bq.oss.corbel.iam.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.bq.oss.corbel.iam.model.Identity;

/**
 * @author Rubén Carrasco
 * 
 */
public interface IdentityRepository extends CrudRepository<Identity, String>, IdentityRepositoryCustom {

    Identity findByOauthIdAndDomainAndOauthService(String oauthId, String domain, String oauthService);

    List<Identity> findByUserIdAndDomain(String username, String domain);

}
