package io.corbel.oauth.api.auth;

import com.google.common.base.Optional;

import io.corbel.oauth.model.Client;
import io.corbel.oauth.repository.ClientRepository;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientCredentialsAuthenticator implements Authenticator<BasicCredentials, Client> {

    private final ClientRepository clientRepository;

    public ClientCredentialsAuthenticator(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Optional<Client> authenticate(BasicCredentials credentials) throws AuthenticationException {
        try {
            Client client = clientRepository.findByName(credentials.getUsername());
            if (client != null && client.getKey().equals(credentials.getPassword())) {
                return Optional.of(client);
            } else {
                return Optional.absent();
            }
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }

}
