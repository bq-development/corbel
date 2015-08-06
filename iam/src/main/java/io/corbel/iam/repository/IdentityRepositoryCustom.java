package io.corbel.iam.repository;

/**
 * @author Rubén Carrasco
 *
 */
public interface IdentityRepositoryCustom {

    boolean existsByDomainAndUserIdAndOauthService(String domain, String username, String oauthService);

    void deleteByUserIdAndDomain(String username, String domain);
}
