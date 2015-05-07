package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;

import com.bq.oss.corbel.iam.exception.DomainAlreadyExists;
import com.bq.oss.corbel.iam.exception.InvalidAggregationException;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.model.Entity;
import com.bq.oss.corbel.iam.repository.DomainRepository;
import com.bq.oss.lib.queries.request.*;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultDomainService implements DomainService {

    private final DomainRepository domainRepository;
    private final ScopeService scopeService;

    public DefaultDomainService(DomainRepository domainRepository, ScopeService scopeService) {
        this.domainRepository = domainRepository;
        this.scopeService = scopeService;
    }

    @Override
    public Optional<Domain> getDomain(String domainId) {
        return Optional.ofNullable(domainRepository.findOne(domainId));
    }

    @Override
    public boolean scopesAllowedInDomain(Set<String> scopes, Domain domain) {
        if (scopes == null || scopes.isEmpty()) {
            return true;
        }
        if (domain.getScopes() == null || domain.getScopes().isEmpty()) {
            return false;
        }

        try {
            Set<String> expandedRequestedScopes = scopeService.expandScopes(scopes).stream().map(Entity::getId).collect(Collectors.toSet());
            Set<String> expandedDomainScopes = scopeService.expandScopes(domain.getScopes()).stream().map(Entity::getId)
                    .collect(Collectors.toSet());
            return expandedDomainScopes.containsAll(expandedRequestedScopes);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean oAuthServiceAllowedInDomain(String oAuthService, Domain domain) {
        if (oAuthService == null) {
            throw new IllegalArgumentException("Missing oAuthService");
        }
        Map<String, String> configuration = domain.getAuthConfigurations().get(oAuthService);
        return configuration != null;
    }

    @Override
    public void insert(Domain domain) throws DomainAlreadyExists {
        try {
            domainRepository.insert(domain);
        } catch (DataIntegrityViolationException e) {
            throw new DomainAlreadyExists(domain.getId());
        }
    }

    @Override
    public void update(Domain domain) {
        domainRepository.patch(domain);
    }


    @Override
    public void delete(String domain) {
        domainRepository.delete(domain);
    }

    @Override
    public List<Domain> getAll(ResourceQuery resourceQuery, Pagination pagination, Sort sort) {
        return domainRepository.find(resourceQuery, pagination, sort);
    }

    @Override
    public AggregationResult getDomainsAggregation(ResourceQuery query, Aggregation aggregation) throws InvalidAggregationException {

        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            throw new InvalidAggregationException();
        }
        return domainRepository.count(query);
    }


}
