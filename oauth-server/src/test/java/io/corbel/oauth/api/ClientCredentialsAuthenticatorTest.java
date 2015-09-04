package io.corbel.oauth.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

import io.corbel.oauth.api.auth.ClientCredentialsAuthenticator;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.repository.ClientRepository;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;

public class ClientCredentialsAuthenticatorTest {

    private ClientCredentialsAuthenticator clientCredentialsAuthenticator;
    private ClientRepository clientRepository;

    @Before
    public void before() {
        Client clientTest = new Client();
        clientTest.setKey("temp-secret");
        clientTest.setName("qa-silkroad");
        clientRepository = mock(ClientRepository.class);
        when(clientRepository.findByName("temp-client")).thenReturn(clientTest);
        clientCredentialsAuthenticator = new ClientCredentialsAuthenticator(clientRepository);
    }

    @Test
    public void authenticateTest() throws AuthenticationException {
        Optional<Client> credentials = clientCredentialsAuthenticator.authenticate(new BasicCredentials("temp-client", "temp-secret"));
        assertThat(credentials.isPresent()).isEqualTo(true);
    }

    @Test
    public void authenticateFailTest() throws AuthenticationException {
        Optional<Client> credentials = clientCredentialsAuthenticator.authenticate(new BasicCredentials("temp-client", "fail-secret"));
        assertThat(credentials.isPresent()).isEqualTo(false);
    }

    @Test
    public void authenticateClientMissingTest() throws AuthenticationException {
        Optional<Client> credentials = clientCredentialsAuthenticator.authenticate(new BasicCredentials("fail-client", "temp-secret"));
        assertThat(credentials.isPresent()).isEqualTo(false);
    }

}
