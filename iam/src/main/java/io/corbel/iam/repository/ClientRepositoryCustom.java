package io.corbel.iam.repository;

import io.corbel.iam.model.Client;
import io.corbel.iam.model.ClientCredential;

/**
 * @author Alexander De Leon
 * 
 */
public interface ClientRepositoryCustom {

    ClientCredential findCredentialById(String id);

    void delete(String domain, String client);

    void insert(Client client);

    void deleteByDomain(String domain);

}
