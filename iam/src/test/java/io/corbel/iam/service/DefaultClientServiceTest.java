package io.corbel.iam.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.google.gson.JsonElement;
import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.JsonAggregationResultsFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.exception.ClientAlreadyExistsException;
import io.corbel.iam.model.Client;
import io.corbel.iam.repository.ClientRepository;

@RunWith(MockitoJUnitRunner.class) public class DefaultClientServiceTest {

    private static final String TEST_ID = "testId";
    private static final String DOMAIN_ID = "domainId";
    private static final String TEST_NAME = "testName";
    @Mock private ClientRepository clientRepository;
    private ClientService clientService;
    private AggregationResultsFactory<JsonElement> aggregationResultsFactory = new JsonAggregationResultsFactory();

    private static Client getClient() {
        Client client = new Client();
        client.setId(TEST_ID);
        client.setName(TEST_NAME);
        return client;
    }

    @Before
    public void setUp() {
        clientService = new DefaultClientService(clientRepository, aggregationResultsFactory);
    }

    @Test
    public void testCreate() throws ClientAlreadyExistsException {
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        clientService.createClient(getClient());
        verify(clientRepository).insert(clientCaptor.capture());
        assertEquals(TEST_NAME, clientCaptor.getValue().getName());
    }

    @Test
    public void testUpdate() {
        Client client = getClient();
        clientService.update(client);
        verify(clientRepository).patch(client);
    }

    @Test
    public void testDelete() {
        clientService.delete(DOMAIN_ID, TEST_ID);
        verify(clientRepository).delete(DOMAIN_ID, TEST_ID);
    }

    @Test
    public void testFind() {
        Client expectedClient = mock(Client.class);

        when(clientRepository.findOne(TEST_ID)).thenReturn(expectedClient);

        Optional<Client> client = clientService.find(TEST_ID);

        verify(clientRepository).findOne(TEST_ID);
        assertTrue(client.isPresent());
        assertEquals(expectedClient, client.get());
    }

    @Test
    public void testFailedFind() {
        when(clientRepository.findOne(TEST_ID)).thenReturn(null);

        Optional<Client> client = clientService.find(TEST_ID);

        verify(clientRepository).findOne(TEST_ID);
        assertFalse(client.isPresent());
    }
}
