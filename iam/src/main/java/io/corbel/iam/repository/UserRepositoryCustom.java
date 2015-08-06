package io.corbel.iam.repository;

/**
 * @author Alexander De Leon
 * 
 */
public interface UserRepositoryCustom {

    String findUserDomain(String id);

    boolean existsByUsernameAndDomain(String username, String domainId);

    boolean existsByEmailAndDomain(String email, String domainId);

    void deleteByDomain(String domainId);

}
