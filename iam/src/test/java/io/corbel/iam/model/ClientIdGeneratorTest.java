package io.corbel.iam.model;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import io.corbel.lib.ws.digest.Digester;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientIdGeneratorTest {

    private static final String TEST_ID = "hash_id";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_NAME = "name";

    @Test
    public void testIdGeneration() {
        Digester digesterMock = mock(Digester.class);
        ClientIdGenerator generator = new ClientIdGenerator(digesterMock);
        Client client = new Client();
        client.setDomain(TEST_DOMAIN);
        client.setName(TEST_NAME);
        when(digesterMock.digest(TEST_DOMAIN + "." + TEST_NAME)).thenReturn(TEST_ID);
        assertThat(generator.generateId(client)).isEqualTo(TEST_ID);
    }

}
