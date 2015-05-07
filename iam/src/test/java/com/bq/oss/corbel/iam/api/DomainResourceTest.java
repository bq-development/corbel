package com.bq.oss.corbel.iam.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bq.oss.corbel.iam.exception.ClientAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.DomainAlreadyExists;
import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.service.ClientService;
import com.bq.oss.corbel.iam.service.DomainService;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;
import com.bq.oss.lib.ws.api.error.GenericExceptionMapper;
import com.bq.oss.lib.ws.api.error.JsonValidationExceptionMapper;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.AuthorizationInfoProvider;
import com.bq.oss.lib.ws.queries.QueryParametersProvider;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import io.dropwizard.testing.junit.ResourceTestRule;

public class DomainResourceTest {
    protected static final String AUTHORIZATION = "Authorization";
    private final static ClientService clientService = mock(ClientService.class);
    private final static DomainService domainService = mock(DomainService.class);
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;
    private static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    private static final QueryParser queryParserMock = mock(QueryParser.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final AuthorizationInfoProvider authorizationInfoProviderSpy = spy(new AuthorizationInfoProvider());
    private final static String ISSUER_DOMAIN_ID = "domain";
    private final static String DOMAIN_ID = "jksdawqqqqdfjdaslkfj";
    private final static String CLIENT_ID = "zsdetzerqdfjdaslkfj";
    private static final String TEST_TOKEN = "xxxx";
    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new DomainResource(clientService, domainService)).addProvider(authorizationInfoProviderSpy)
            .addProvider(new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, queryParserMock, aggregationParserMock))
            .addProvider(GenericExceptionMapper.class).addProvider(JsonValidationExceptionMapper.class).build();

    public DomainResourceTest() throws Exception {
        when(authorizationInfoMock.getClientId()).thenReturn(CLIENT_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(ISSUER_DOMAIN_ID);
        doReturn(authorizationInfoMock).when(authorizationInfoProviderSpy).getValue(any());
    }

    @Before
    public void setup() {
        reset(clientService, domainService);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);
    }

    private Domain getDomain() {
        Domain domain = new Domain();
        domain.setId(DOMAIN_ID);
        return domain;
    }

    @Test
    public void testCreateDomain() throws DomainAlreadyExists {
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);

        ClientResponse response = RULE.client().resource("/v1.0/domain").type(MediaType.APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, getDomain());

        verify(domainService).insert(domainCaptor.capture());
        assertEquals(ISSUER_DOMAIN_ID + ":" + DOMAIN_ID, domainCaptor.getValue().getId());
        assertEquals(ClientResponse.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaders().getFirst("Location").endsWith(DOMAIN_ID));
    }

    @Test
    public void testEmptyCreateDomain() throws DomainAlreadyExists {
        ClientResponse response = RULE.client().resource("/v1.0/domain").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, new Domain());
        assertEquals(422, response.getStatus());
    }

    @Test
    public void testCreateAlreadyExistingDomain() throws DomainAlreadyExists {
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);

        doThrow(DomainAlreadyExists.class).when(domainService).insert(Mockito.<Domain>any());

        ClientResponse response = RULE.client().resource("/v1.0/domain").type(MediaType.APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(ClientResponse.class, getDomain());

        verify(domainService).insert(domainCaptor.capture());
        assertEquals(ISSUER_DOMAIN_ID + ":" + DOMAIN_ID, domainCaptor.getValue().getId());
        assertEquals(ClientResponse.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetDomain() {
        Domain expectedDomain = getDomain();

        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.ofNullable(expectedDomain));

        Domain domain = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID).accept(MediaType.APPLICATION_JSON_TYPE).get(Domain.class);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        assertEquals(DOMAIN_ID, domain.getId());
    }

    @Test
    public void testGetAllDomain() {
        Domain expectedDomain = getDomain();
        List<Domain> domains = new ArrayList<>();
        domains.add(expectedDomain);

        Pagination defaultPagination = new Pagination(0, DEFAULT_LIMIT);

        when(domainService.getAll(Mockito.any(ResourceQuery.class), Mockito.any(Pagination.class), Mockito.any(Sort.class))).thenReturn(
                domains);

        List<Domain> domainsResponse = RULE.client().resource("/v1.0/domain").type(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Domain>>() {
                });

        verify(domainService).getAll(null, defaultPagination, null);
        assertEquals(domains, domainsResponse);
    }

    @Test
    public void testGetUnknownDomain() {
        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.empty());

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        assertEquals(ClientResponse.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUpdateDomain() {
        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID).type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, getDomain());

        verify(domainService).update(domainCaptor.capture());
        assertEquals(DOMAIN_ID, domainCaptor.getValue().getId());
        assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteDomain() {
        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID).type(MediaType.APPLICATION_JSON_TYPE)
                .delete(ClientResponse.class);

        verify(domainService).delete(eq(DOMAIN_ID));
        assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    private Client getClient() {
        Client client = new Client();
        client.setName("kljsfdkl");
        client.setKey("sjiaedfoa");
        client.setDomain(DOMAIN_ID);
        client.setId(CLIENT_ID);

        return client;
    }

    @Test
    public void testCreateClient() throws ClientAlreadyExistsException {
        Client client = getClient();

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);

        Domain mockDomain = mock(Domain.class);
        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.of(mockDomain));
        when(domainService.scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain))).thenReturn(true);

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, client);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        verify(domainService).scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain));
        verify(clientService).createClient(clientCaptor.capture());
        verifyNoMoreInteractions(domainService, clientService);

        assertEquals(CLIENT_ID, clientCaptor.getValue().getId());
        assertEquals(DOMAIN_ID, clientCaptor.getValue().getDomain());
        assertEquals(ClientResponse.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaders().getFirst("Location").endsWith(CLIENT_ID));
    }

    @Test
    public void testCreateClientWithNonexistentDomain() throws ClientAlreadyExistsException {
        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.empty());

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, getClient());

        verify(domainService).getDomain(eq(DOMAIN_ID));
        verifyNoMoreInteractions(domainService, clientService);

        assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateClientWithMoreScopesThanDomain() throws ClientAlreadyExistsException {
        Client client = getClient();

        Domain mockDomain = mock(Domain.class);
        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.of(mockDomain));
        when(domainService.scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain))).thenReturn(false);

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, client);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        verify(domainService).scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain));
        verifyNoMoreInteractions(domainService, clientService);

        assertEquals(ClientResponse.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetClient() {
        Client expectedClient = getClient();

        when(clientService.find(eq(CLIENT_ID))).thenReturn(Optional.ofNullable(expectedClient));

        Client client = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client/" + CLIENT_ID)
                .accept(MediaType.APPLICATION_JSON_TYPE).get(Client.class);

        verify(clientService).find(eq(CLIENT_ID));
        assertEquals(CLIENT_ID, client.getId());
    }

    @Test
    public void testGetClientByDomain() {
        Client expectedClient = getClient();
        List<Client> clientList = new ArrayList<>();
        clientList.add(expectedClient);

        Pagination defaultPagination = new Pagination(0, DEFAULT_LIMIT);

        when(clientService.findClientsByDomain(eq(DOMAIN_ID), any(ResourceQuery.class), any(Pagination.class), any(Sort.class)))
                .thenReturn(clientList);

        List<Client> clients = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client").accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Client>>() {
                });

        verify(clientService).findClientsByDomain(DOMAIN_ID, null, defaultPagination, null);
        assertEquals(clients, clientList);
    }

    @Test
    public void testGetUnknownClient() {
        when(clientService.find(eq(CLIENT_ID))).thenReturn(Optional.empty());

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client/" + CLIENT_ID)
                .accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

        verify(clientService).find(eq(CLIENT_ID));
        assertEquals(ClientResponse.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetClientOfAnotherDomain() {
        Client expectedClient = getClient();
        expectedClient.setDomain("asdjlkrfasjkl");

        when(clientService.find(eq(CLIENT_ID))).thenReturn(Optional.ofNullable(expectedClient));

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client/" + CLIENT_ID)
                .accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

        verify(clientService).find(eq(CLIENT_ID));
        assertEquals(ClientResponse.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testModifyClient() {
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);

        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client/" + CLIENT_ID)
                .type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, getClient());

        verify(clientService).update(clientCaptor.capture());
        assertEquals(CLIENT_ID, clientCaptor.getValue().getId());
        assertEquals(DOMAIN_ID, clientCaptor.getValue().getDomain());
        assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteClient() {
        ClientResponse response = RULE.client().resource("/v1.0/domain/" + DOMAIN_ID + "/client/" + CLIENT_ID).delete(ClientResponse.class);

        verify(clientService).delete(eq(DOMAIN_ID), eq(CLIENT_ID));
        assertEquals(ClientResponse.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}
