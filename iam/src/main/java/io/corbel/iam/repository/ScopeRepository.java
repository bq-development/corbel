package io.corbel.iam.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import io.corbel.iam.model.Scope;

/**
 * @author Alexander De Leon
 * 
 */
public interface ScopeRepository extends CrudRepository<Scope, String> {
    String SCOPE_CACHE = "scope_cache";

    @Cacheable(SCOPE_CACHE)
    Scope findOne(String id);

}
