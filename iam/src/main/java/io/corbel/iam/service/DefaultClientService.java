package io.corbel.iam.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.iam.exception.ClientAlreadyExistsException;
import io.corbel.iam.exception.InvalidAggregationException;
import io.corbel.iam.model.Client;
import io.corbel.iam.repository.ClientRepository;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.request.*;

public class DefaultClientService implements ClientService {
    private ClientRepository clientRepository;
    private AggregationResultsFactory<JsonElement> aggregationResultsFactory;

    public DefaultClientService(ClientRepository clientRepository, AggregationResultsFactory aggregationResultsFactory) {
        this.clientRepository = clientRepository;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @Override
    public void createClient(Client client) throws ClientAlreadyExistsException {
        client.setId(null);

        if (client.getKey() == null) {
            client.setKey(generateRandomKey());
        }

        try {
            clientRepository.insert(client);
        } catch (DataIntegrityViolationException e) {
            throw new ClientAlreadyExistsException();
        }
    }

    @Override
    public void update(Client client) {
        clientRepository.patch(client);
    }

    @Override
    public void delete(String domain, String client) {
        clientRepository.delete(domain, client);
    }

    @Override
    public Optional<Client> find(String clientId) {
        return Optional.ofNullable(clientRepository.findOne(clientId));
    }

    @Override
    public List<Client> findClientsByDomain(String domainId, ResourceQuery query, Pagination pagination, Sort sort) {
        return clientRepository.find(addDomainToQuery(domainId, query), pagination, sort);
    }

    @Override
    public JsonElement getClientsAggregation(String domainId, ResourceQuery query, Aggregation aggregation)
            throws InvalidAggregationException {

        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            throw new InvalidAggregationException();
        }
        return aggregationResultsFactory.countResult(clientRepository.count(addDomainToQuery(domainId, query)));

    }

    private ResourceQuery addDomainToQuery(String domain, ResourceQuery resourceQuery) {
        ResourceQueryBuilder builder = new ResourceQueryBuilder(resourceQuery);
        builder.add("domain", domain);
        return builder.build();
    }

    private String generateRandomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        StringBuilder result = new StringBuilder();
        for (final byte element : key) {
            result.append(Integer.toString((element & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
