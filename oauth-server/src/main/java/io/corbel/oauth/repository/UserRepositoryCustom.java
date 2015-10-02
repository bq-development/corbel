package io.corbel.oauth.repository;

/**
 * @author Ricardo Martínez
 */
public interface UserRepositoryCustom {

    boolean existsByUsernameAndDomain(String username, String domainId);

}
