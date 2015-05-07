package com.bq.oss.corbel.iam.repository;

import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.model.ClientCredential;

/**
 * @author Alexander De Leon
 * 
 */
public interface ClientRepositoryCustom {

    ClientCredential findCredentialById(String id);

    void delete(String domain, String client);

    void insert(Client client);
}
