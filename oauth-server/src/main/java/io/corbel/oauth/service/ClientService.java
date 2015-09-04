package io.corbel.oauth.service;

import java.util.Optional;

import io.corbel.oauth.model.Client;

/**
 * @author Rubén Carrasco
 * 
 */
public interface ClientService {
    Optional<Client> findByName(String name);

    boolean verifyRedirectUri(String uri, Client client);

    boolean verifyClientSecret(String clientSecret, Client client);
}
