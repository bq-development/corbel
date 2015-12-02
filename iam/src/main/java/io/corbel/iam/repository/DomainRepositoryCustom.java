package io.corbel.iam.repository;

import io.corbel.iam.model.Domain;

/**
 * @author Cristian del Cerro
 */
public interface DomainRepositoryCustom {

    void addDefaultScopes(String id, String... scopes);

    void removeDefaultScopes(String id, String... scopes);

    void addPublicScopes(String id, String... scopes);

    void removePublicScopes(String id, String... scopes);

    void insert(Domain domain);
}
