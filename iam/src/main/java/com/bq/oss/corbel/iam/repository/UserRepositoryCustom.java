package com.bq.oss.corbel.iam.repository;

/**
 * @author Alexander De Leon
 * 
 */
public interface UserRepositoryCustom {

    String findUserDomain(String id);

    boolean existsByUsernameAndDomain(String username, String domainId);
}
