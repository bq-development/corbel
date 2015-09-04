package io.corbel.oauth.repository;

import org.springframework.data.repository.CrudRepository;

import io.corbel.oauth.model.Client;

/**
 * @author Rubén Carrasco
 */
public interface ClientRepository extends CrudRepository<Client, String> {
    public Client findByName(String name);
}
