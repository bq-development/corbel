package io.corbel.iam.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.JsonObject;

import io.corbel.iam.exception.ClientAlreadyExistsException;
import io.corbel.iam.model.Client;
import io.corbel.iam.model.Domain;
import io.corbel.iam.service.ClientService;
import io.corbel.iam.service.DomainService;
import io.corbel.lib.queries.parser.*;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.api.error.JsonValidationExceptionMapper;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.auth.AuthorizationInfoProvider;
import io.corbel.lib.ws.auth.AuthorizationRequestFilter;
import io.corbel.lib.ws.auth.CookieOAuthFactory;
import io.corbel.lib.ws.queries.QueryParametersProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

public class ClientResourceTest {

    private static final ClientService clientService = mock(ClientService.class);
    private static final DomainService domainService = mock(DomainService.class);
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;

    private static final SortParser sortParserMock = mock(SortParser.class);
    private static final SearchParser searchParserMock = mock(SearchParser.class);
    private static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    private static final PaginationParser paginationParserMock = mock(PaginationParser.class);
    private static final QueryParser queryParserMock = mock(QueryParser.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
    private static final String ISSUER_DOMAIN_ID = "domain";
    private static final String DOMAIN_ID = "jksdawqqqqdfjdaslkfj";
    private static final String CLIENT_ID = "zsdetzerqdfjdaslkfj";
    private static final String TEST_TOKEN = "xxxx";

    private static final Authenticator<String, AuthorizationInfo> authenticator = mock(Authenticator.class);
    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticator, "realm", AuthorizationInfo.class);
    private static CookieOAuthFactory<AuthorizationInfo> cookieOAuthProvider = new CookieOAuthFactory<AuthorizationInfo>(authenticator,
            "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, cookieOAuthProvider, "", false, "client"));

    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addResource(new ClientResource(clientService, domainService))
            .addProvider(filter)
            .addProvider(new AuthorizationInfoProvider().getBinder())
            .addProvider(
                    new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, new QueryParametersParser(queryParserMock,
                            aggregationParserMock, sortParserMock, paginationParserMock, searchParserMock)).getBinder())
            .addProvider(GenericExceptionMapper.class).addProvider(JsonValidationExceptionMapper.class).build();

    public ClientResourceTest() throws Exception {
        when(authorizationInfoMock.getClientId()).thenReturn(CLIENT_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(ISSUER_DOMAIN_ID);
        when(authorizationInfoMock.getAccessRules()).thenReturn(new HashSet<>());

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);

        when(authenticator.authenticate(TEST_TOKEN)).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
    }

    @Before
    public void setup() {
        reset(clientService, domainService);
        when(authorizationInfoMock.getToken()).thenReturn(TEST_TOKEN);
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

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(client), Response.class);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        verify(domainService).scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain));
        verify(clientService).createClient(clientCaptor.capture());
        verifyNoMoreInteractions(domainService, clientService);

        assertEquals(CLIENT_ID, clientCaptor.getValue().getId());
        assertEquals(DOMAIN_ID, clientCaptor.getValue().getDomain());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").endsWith(CLIENT_ID));
    }

    @Test
    public void testCreateClientWithNonexistentDomain() throws ClientAlreadyExistsException {
        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.empty());

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(getClient()), Response.class);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        verifyNoMoreInteractions(domainService, clientService);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateClientWithMoreScopesThanDomain() throws ClientAlreadyExistsException {
        Client client = getClient();

        Domain mockDomain = mock(Domain.class);
        when(domainService.getDomain(eq(DOMAIN_ID))).thenReturn(Optional.of(mockDomain));
        when(domainService.scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain))).thenReturn(false);

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(client), Response.class);

        verify(domainService).getDomain(eq(DOMAIN_ID));
        verify(domainService).scopesAllowedInDomain(eq(client.getScopes()), eq(mockDomain));
        verifyNoMoreInteractions(domainService, clientService);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetClient() {
        Client expectedClient = getClient();

        when(clientService.find(eq(CLIENT_ID))).thenReturn(Optional.ofNullable(expectedClient));

        Client client = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client/" + CLIENT_ID).request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Client.class);

        verify(clientService).find(eq(CLIENT_ID));
        assertEquals(CLIENT_ID, client.getId());
    }

    @Test
    public void testGetClientByDomain() {
        Client expectedClient = getClient();
        List<Client> clientList = new ArrayList<>();
        clientList.add(expectedClient);

        Pagination defaultPagination = new Pagination(0, DEFAULT_LIMIT);
        when(paginationParserMock.parse(0, DEFAULT_LIMIT, MAX_DEFAULT_LIMIT)).thenReturn(defaultPagination);
        when(clientService.findClientsByDomain(eq(DOMAIN_ID), any(ResourceQuery.class), any(Pagination.class), any(Sort.class)))
                .thenReturn(clientList);

        List<Client> clients = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client").request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).get(new GenericType<List<Client>>() {
                });

        verify(clientService).findClientsByDomain(DOMAIN_ID, null, defaultPagination, null);
        assertEquals(clients, clientList);
    }

    @Test
    public void testGetUnknownClient() {
        when(clientService.find(eq(CLIENT_ID))).thenReturn(Optional.empty());

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client/" + CLIENT_ID)
                .request(MediaType.APPLICATION_JSON_TYPE).header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        verify(clientService).find(eq(CLIENT_ID));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetClientOfAnotherDomain() {
        Client expectedClient = getClient();
        expectedClient.setDomain("asdjlkrfasjkl");

        when(clientService.find(eq(CLIENT_ID))).thenReturn(Optional.of(expectedClient));

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client/" + CLIENT_ID)
                .request(MediaType.APPLICATION_JSON_TYPE).header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).get(Response.class);

        verify(clientService).find(eq(CLIENT_ID));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testModifyClient() {
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client/" + CLIENT_ID).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).put(Entity.json(getClient()), Response.class);

        verify(clientService).update(clientCaptor.capture());
        assertEquals(CLIENT_ID, clientCaptor.getValue().getId());
        assertEquals(DOMAIN_ID, clientCaptor.getValue().getDomain());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteClient() {
        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/client/" + CLIENT_ID).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(Response.class);

        verify(clientService).delete(eq(DOMAIN_ID), eq(CLIENT_ID));
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}
