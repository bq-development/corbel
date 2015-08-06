package io.corbel.iam.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.testing.junit.ResourceTestRule;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.corbel.iam.exception.ScopeNameException;
import io.corbel.iam.model.Scope;
import io.corbel.iam.service.ClientService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.ScopeService;

public class ScopeResourceTest {
    private final static ScopeService scopeService = mock(ScopeService.class);
    private final static ClientService clientService = mock(ClientService.class);
    private final static DomainService domainService = mock(DomainService.class);

    private final static String SCOPE_ID = "testId";

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new ScopeResource(scopeService)).build();

    @Before
    public void setup() {
        reset(clientService, domainService, scopeService);
    }

    @Test
    public void testCreateScopeWithoutParameters() throws ScopeNameException {

        String scope = "{\"id\": \"testId\", \"audience\" : \"test\", \"rules\": [{\"rule\":\"rule\"}, {\"rule2\":\"rule2\"}]}";

        ArgumentCaptor<Scope> scopeCaptor = ArgumentCaptor.forClass(Scope.class);

        Response response = RULE.client().target("/v1.0/scope").request().post(Entity.json(scope), Response.class);

        verify(scopeService).create(scopeCaptor.capture());
        assertEquals(SCOPE_ID, scopeCaptor.getValue().getId());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").endsWith(SCOPE_ID));
    }

    @Test
    public void testCreateScopeWithParameters() throws ScopeNameException {

        String scope = "{\"id\": \"testId\", \"audience\" : \"test\", \"rules\": [{\"rule\":\"rule\"}, "
                + "{\"rule2\":\"rule2\"}], \"parameters\": { \"resourceId\" : \"id\"}}";

        ArgumentCaptor<Scope> scopeCaptor = ArgumentCaptor.forClass(Scope.class);

        Response response = RULE.client().target("/v1.0/scope").request().post(Entity.json(scope), Response.class);

        verify(scopeService).create(scopeCaptor.capture());
        assertEquals(SCOPE_ID, scopeCaptor.getValue().getId());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").endsWith(SCOPE_ID));
    }

    @Test
    public void testCreateBadScope() throws ScopeNameException {

        String scope = "{\"id\": \"testId\", \"audience\" : \"test\", \"rules\": [{\"rule\":\"rule\"}, {\"rule2\":\"rule2\"}]}";

        ArgumentCaptor<Scope> scopeCaptor = ArgumentCaptor.forClass(Scope.class);

        doThrow(ScopeNameException.class).when(scopeService).create(any());

        Response response = RULE.client().target("/v1.0/scope").request().post(Entity.json(scope), Response.class);

        verify(scopeService).create(scopeCaptor.capture());
        assertEquals(SCOPE_ID, scopeCaptor.getValue().getId());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetScope() {
        Scope expectedScope = new Scope(SCOPE_ID, null, null, null, null, null);


        when(scopeService.getScope(SCOPE_ID)).thenReturn(expectedScope);

        Scope scope = RULE.client().target("/v1.0/scope/" + SCOPE_ID).request(MediaType.APPLICATION_JSON_TYPE).get(Scope.class);

        verify(scopeService).getScope(eq(SCOPE_ID));
        assertEquals(scope, expectedScope);
    }

    @Test
    public void testGetUnknownScope() {
        when(scopeService.getScope(SCOPE_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/scope/" + SCOPE_ID).request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

        verify(scopeService).getScope(eq(SCOPE_ID));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteScope() {
        Response response = RULE.client().target("/v1.0/scope/" + SCOPE_ID).request().delete(Response.class);

        verify(scopeService).delete(eq(SCOPE_ID));
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}