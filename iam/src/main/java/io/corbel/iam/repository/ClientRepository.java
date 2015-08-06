package io.corbel.iam.repository;

import java.util.List;

import io.corbel.iam.model.Client;
import io.corbel.lib.mongo.repository.PartialUpdateRepository;
import io.corbel.lib.queries.mongo.repository.GenericFindRepository;

/**
 * @author Alberto J. Rubio
 */
public interface ClientRepository extends PartialUpdateRepository<Client, String>, GenericFindRepository<Client, String>,
        ClientRepositoryCustom, HasScopesRepository<String> {

    List<Client> findByDomain(String domainId);

    String COLLECTION = "client";
    String FIELD_SIGNATURE_ALGORITHM = "signatureAlgorithm";
    String FIELD_KEY = "key";
    String FIELD_ID = "_id";
    String FIELD_DOMAIN = "domain";


}
