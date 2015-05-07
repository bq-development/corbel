package com.bq.oss.corbel.iam.repository;

import com.bq.oss.corbel.iam.model.Domain;

/**
 * @author Cristian del Cerro
 */
public interface DomainRepositoryCustom {

    void addDefaultScopes(String id, String... scopes);

    void removeDefaultScopes(String id, String... scopes);

    void insert(Domain domain);
}
