package io.corbel.iam.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import io.corbel.iam.model.Client;
import io.corbel.iam.model.ClientCredential;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientRepositoryImpl extends HasScopesRepositoryBase<Client, String>implements ClientRepositoryCustom {

    private static final String FIELD_DOMAIN = "domain";

    private final MongoOperations mongo;

    @Autowired
    public ClientRepositoryImpl(MongoOperations mongo) {
        super(mongo, Client.class);
        this.mongo = mongo;
    }

    @Override
    public ClientCredential findCredentialById(String id) {
        Criteria criteria = where(FIELD_ID).is(id);
        Query query = query(criteria);
        query.fields().include(ClientRepository.FIELD_SIGNATURE_ALGORITHM).include(ClientRepository.FIELD_KEY);
        return mongo.findOne(query, ClientCredential.class, ClientRepository.COLLECTION);
    }

    @Override
    public void delete(String domain, String client) {
        mongo.findAndRemove(query(where("id").is(client).and(FIELD_DOMAIN).is(domain)), Client.class);
    }

    @Override
    public void insert(Client client) {
        mongo.insert(client);
    }

    @Override
    public void deleteByDomain(String domain) {
        mongo.remove(query(where(FIELD_DOMAIN).is(domain)), Client.class);
    }

}
