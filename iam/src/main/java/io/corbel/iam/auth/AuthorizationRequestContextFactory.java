package io.corbel.iam.auth;

import net.oauth.jsontoken.JsonToken;

import io.corbel.iam.repository.ClientRepository;
import io.corbel.iam.repository.DomainRepository;
import io.corbel.iam.repository.UserRepository;

/**
 * @author Alexander De Leon
 * 
 */
public class AuthorizationRequestContextFactory {

    private final ClientRepository clientRepository;
    private final DomainRepository domainRepository;
    private final UserRepository userRepository;

    public AuthorizationRequestContextFactory(ClientRepository clientRepository, DomainRepository domainRepository,
            UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
    }

    public AuthorizationRequestContext fromJsonToken(JsonToken jsonToken) {
        return new JsonTokenAuthorizationRequestContext(clientRepository, domainRepository, userRepository, jsonToken);
    }
}
