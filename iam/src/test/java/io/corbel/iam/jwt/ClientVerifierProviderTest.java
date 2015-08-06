package io.corbel.iam.jwt;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import io.corbel.iam.model.Client;
import io.corbel.iam.model.ClientCredential;
import io.corbel.iam.model.SignatureAlgorithm;
import io.corbel.iam.repository.ClientRepository;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientVerifierProviderTest {

    private static final String TEST_CLIENT_ID = null;
    private static final String INVALID_PUBLIC_KEY = "~";
    private static final String VALID_HMAC_KEY = "0xb613679a0814d9ec772f95d778c35fc5ff1697c493715653c6c712144292c5ad";
    private static final String VALID_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw0Omx8B47DwSoS1jtgBE7LHoZlDWlFCvISkmgJ/Rl6llmtaW0Bd1RGfRrEGLCn4CuP7CTrLH/x9Go+cZEL+DpeuCEU3wQ3zJJwjjWNnrvNGXftNiBBAdYHdhdUluLJ2GmE8n+ntWh4GcG9YD9zJg5jMKKl3MQA6YDE1mI5tl0kwIDAQAB";
    public ClientRepository clientRepositoryMock;
    public ClientVerifierProvider clientVerifierProvider;

    @Before
    public void setup() {
        clientRepositoryMock = mock(ClientRepository.class);
        clientVerifierProvider = new ClientVerifierProvider(clientRepositoryMock);
    }

    @Test
    public void testClientNotFound() {
        when(clientRepositoryMock.findOne(TEST_CLIENT_ID)).thenReturn(null);
        assertThat(clientVerifierProvider.findVerifier(TEST_CLIENT_ID, null)).isNull();
    }

    @Test
    public void testInvalidKey() {
        Client client = mock(Client.class);
        when(client.getSignatureAlgorithm()).thenReturn(SignatureAlgorithm.RS256);
        when(client.getKey()).thenReturn(INVALID_PUBLIC_KEY);
        when(clientRepositoryMock.findOne(TEST_CLIENT_ID)).thenReturn(client);
        assertThat(clientVerifierProvider.findVerifier(TEST_CLIENT_ID, null)).isNull();
    }

    @Test
    public void testValidPublicKey() {
        ClientCredential credential = mock(ClientCredential.class);
        when(credential.getSignatureAlgorithm()).thenReturn(SignatureAlgorithm.RS256);
        when(credential.getKey()).thenReturn(VALID_PUBLIC_KEY);
        when(clientRepositoryMock.findCredentialById(TEST_CLIENT_ID)).thenReturn(credential);
        assertThat(clientVerifierProvider.findVerifier(TEST_CLIENT_ID, null)).isNotNull();
    }

    @Test
    public void testValidHmacKey() {
        ClientCredential credential = mock(ClientCredential.class);
        when(credential.getSignatureAlgorithm()).thenReturn(SignatureAlgorithm.HS256);
        when(credential.getKey()).thenReturn(VALID_HMAC_KEY);
        when(clientRepositoryMock.findCredentialById(TEST_CLIENT_ID)).thenReturn(credential);
        assertThat(clientVerifierProvider.findVerifier(TEST_CLIENT_ID, null)).isNotNull();
    }
}
