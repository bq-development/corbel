package io.corbel.iam.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;
import io.corbel.iam.exception.DomainAlreadyExists;
import io.corbel.iam.exception.InvalidAggregationException;
import io.corbel.iam.model.Domain;
import io.corbel.lib.queries.request.*;

/**
 * @author Alexander De Leon
 * 
 */
public interface DomainService {

    public Optional<Domain> getDomain(String domainId);

    /**
     * Checks if the specified scopes are allows between the domain identified by the given id.
     * 
     * This method is null safe, if the specified scopes are null or empty the result is always true.
     * 
     * @param scopes Scopes to check.
     * @param domain Domain to check.
     * @return true if scopes are allowed.
     * @throws IllegalArgumentException if the specified domainId does not correspond to an existing domain.
     */
    boolean scopesAllowedInDomain(Set<String> scopes, Domain domain);

    boolean oAuthServiceAllowedInDomain(String oAuthService, Domain domain);

    void insert(Domain domain) throws DomainAlreadyExists;

    void update(Domain domain);

    void delete(String domain);

    List<Domain> getAll(ResourceQuery resourceQuery, Pagination pagination, Sort sort);

    JsonElement getDomainsAggregation(ResourceQuery query, Aggregation aggregation) throws InvalidAggregationException;

}
