package io.corbel.oauth.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.corbel.oauth.model.Client;
import io.corbel.oauth.repository.ClientRepository;

/**
 * @author Rubén Carrasco
 * 
 */
public class DefaultClientService implements ClientService {

    private final ClientRepository clientRepository;

    public DefaultClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Optional<Client> findByName(String name) {
        return Optional.ofNullable(clientRepository.findByName(name));
    }

    @Override
    public boolean verifyRedirectUri(String uri, Client client) {
        if (client != null) {
            Pattern pattern = Pattern.compile(client.getRedirectRegexp());
            Matcher matcher = pattern.matcher(uri);
            return matcher.find();
        }
        return false;
    }

    @Override
    public boolean verifyClientSecret(String clientSecret, Client client) {
        return client != null && client.getKey().equals(clientSecret);
    }

}
