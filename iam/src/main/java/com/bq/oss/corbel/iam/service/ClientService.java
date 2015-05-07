package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;

import com.bq.oss.corbel.iam.exception.ClientAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.InvalidAggregationException;
import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.lib.queries.request.*;

public interface ClientService {
    void createClient(Client client) throws ClientAlreadyExistsException;

    void update(Client client);

    void delete(String domain, String client);

    Optional<Client> find(String clientId);

    List<Client> findClientsByDomain(String domainId, ResourceQuery query, Pagination pagination, Sort sort);

    AggregationResult getClientsAggregation(String domainId, ResourceQuery query, Aggregation aggregation)
            throws InvalidAggregationException;
}
