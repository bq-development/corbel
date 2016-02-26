package io.corbel.iam.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.iam.exception.DomainAlreadyExists;
import io.corbel.iam.exception.InvalidAggregationException;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Scope;
import io.corbel.iam.repository.DomainRepository;
import io.corbel.lib.queries.request.*;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultDomainService implements DomainService {

    private final DomainRepository domainRepository;
    private final ScopeService scopeService;
    private final EventsService eventsService;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    public DefaultDomainService(DomainRepository domainRepository, ScopeService scopeService, EventsService eventsService, AggregationResultsFactory<JsonElement> aggregationResultsFactory) {
        this.domainRepository = domainRepository;
        this.scopeService = scopeService;
        this.eventsService = eventsService;
        this.aggregationResultsFactory = aggregationResultsFactory;
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
            Set<String> expandedRequestedScopes = scopeService.expandScopes(scopes).stream().map(Scope::getIdWithParameters)
                    .collect(Collectors.toSet());
            Set<String> expandedDomainScopes = scopeService.expandScopes(domain.getScopes()).stream().map(Scope::getIdWithParameters)
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
            sendUpdateDomainPublicScopesEvent(domain.getId());
        } catch (DataIntegrityViolationException e) {
            throw new DomainAlreadyExists(domain.getId());
        }
    }

    @Override
    public void update(Domain domain) {
        domainRepository.patch(domain);
        sendUpdateDomainPublicScopesEvent(domain.getId());
    }


    @Override
    public void delete(String domain) {
        domainRepository.delete(domain);
        sendUpdateDomainPublicScopesEvent(domain);
        eventsService.sendDomainDeletedEvent(domain);
    }

    @Override
    public List<Domain> getAll(ResourceQuery resourceQuery, Pagination pagination, Sort sort) {
        return domainRepository.find(resourceQuery, pagination, sort);
    }

    @Override
    public JsonElement getDomainsAggregation(ResourceQuery query, Aggregation aggregation)
            throws InvalidAggregationException {

        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            throw new InvalidAggregationException();
        }
        return aggregationResultsFactory.countResult(domainRepository.count(query));
    }

    private void sendUpdateDomainPublicScopesEvent(String domainId) {
        eventsService.sendUpdateDomainPublicScopesEvent(domainId);
    }

}
