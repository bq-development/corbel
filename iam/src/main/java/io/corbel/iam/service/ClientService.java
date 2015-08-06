package io.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import io.corbel.iam.exception.ClientAlreadyExistsException;
import io.corbel.iam.exception.InvalidAggregationException;
import io.corbel.iam.model.Client;
import io.corbel.lib.queries.request.*;

public interface ClientService {
    void createClient(Client client) throws ClientAlreadyExistsException;

    void update(Client client);

    void delete(String domain, String client);

    Optional<Client> find(String clientId);

    List<Client> findClientsByDomain(String domainId, ResourceQuery query, Pagination pagination, Sort sort);

    AggregationResult getClientsAggregation(String domainId, ResourceQuery query, Aggregation aggregation)
            throws InvalidAggregationException;
}
