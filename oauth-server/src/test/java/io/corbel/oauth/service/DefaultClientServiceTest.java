package io.corbel.oauth.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import io.corbel.oauth.model.Client;
import io.corbel.oauth.repository.ClientRepository;

/**
 * @author Rubén Carrasco
 * 
 */
public class DefaultClientServiceTest {

    private static final String TEST_REDIRECT_URI = "http://example.org?a=b#frag";
    private static final String TEST_REDIRECT_URI2 = "http://example.org/user?a=b#frag";
    private static final String TEST_REDIRECT_URI_REGEX = "http://example.org/*.*";
    private static final String TEST_CLIENT_ID = "TEST_CLIENT";
    private static Client TEST_CLIENT;

    private ClientRepository clientRepository;
    private ClientService clientService;

    @Before
    public void setup() {
        clientRepository = mock(ClientRepository.class);
        TEST_CLIENT = new Client();
        TEST_CLIENT.setRedirectRegexp(TEST_REDIRECT_URI_REGEX);
        clientService = new DefaultClientService(clientRepository);
    }

    @Test
    public void testRegexp() {
        assertThat(clientService.verifyRedirectUri(TEST_REDIRECT_URI, TEST_CLIENT)).isTrue();
        assertThat(clientService.verifyRedirectUri(TEST_REDIRECT_URI2, TEST_CLIENT)).isTrue();
    }
}
