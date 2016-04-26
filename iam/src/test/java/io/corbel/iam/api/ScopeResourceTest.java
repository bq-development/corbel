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

import com.google.gson.JsonObject;
import io.corbel.iam.exception.ScopeAbsentIdException;
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

import java.util.HashSet;

public class ScopeResourceTest {
    private static final ScopeService scopeService = mock(ScopeService.class);
    private static final ClientService clientService = mock(ClientService.class);
    private static final DomainService domainService = mock(DomainService.class);

    private static final String DOMAIN_ID = "domainId";
    private static final String SCOPE_ID = "testId";
    //TODO: Fix login with scopes
    //private static final String SCOPE_ID_WITH_DOMAIN = DOMAIN_ID + Scope.ID_SEPARATOR + SCOPE_ID;
    private static final String SCOPE_ID_WITH_DOMAIN = SCOPE_ID;

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new ScopeResource(scopeService)).build();

    @Before
    public void setup() {
        reset(clientService, domainService, scopeService);
    }

    @Test
    public void testCreateScopeWithoutParameters() throws ScopeNameException, ScopeAbsentIdException {

        String scope = "{\"id\": \"testId\", \"audience\" : \"test\", \"rules\": [{\"rule\":\"rule\"}, {\"rule2\":\"rule2\"}]}";

        ArgumentCaptor<Scope> scopeCaptor = ArgumentCaptor.forClass(Scope.class);

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/scope").request()
                            .post(Entity.json(scope), Response.class);

        verify(scopeService).create(scopeCaptor.capture());
        assertEquals(SCOPE_ID_WITH_DOMAIN, scopeCaptor.getValue().getId());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").endsWith(SCOPE_ID));
    }

    @Test
    public void testCreateScopeWithParameters() throws ScopeNameException, ScopeAbsentIdException {

        String scope = "{\"id\": \"testId\", \"audience\" : \"test\", \"rules\": [{\"rule\":\"rule\"}, "
                + "{\"rule2\":\"rule2\"}], \"parameters\": { \"resourceId\" : \"id\"}}";

        ArgumentCaptor<Scope> scopeCaptor = ArgumentCaptor.forClass(Scope.class);

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/scope").request()
                            .post(Entity.json(scope), Response.class);

        verify(scopeService).create(scopeCaptor.capture());
        assertEquals(SCOPE_ID_WITH_DOMAIN, scopeCaptor.getValue().getId());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getHeaderString("Location").endsWith(SCOPE_ID));
    }

    @Test
    public void testCreateBadScope() throws ScopeNameException, ScopeAbsentIdException {

        String scope = "{\"id\": \"testId\", \"audience\" : \"test\", \"rules\": [{\"rule\":\"rule\"}, {\"rule2\":\"rule2\"}]}";

        ArgumentCaptor<Scope> scopeCaptor = ArgumentCaptor.forClass(Scope.class);

        doThrow(ScopeNameException.class).when(scopeService).create(any());

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/scope").request()
                            .post(Entity.json(scope), Response.class);

        verify(scopeService).create(scopeCaptor.capture());
        assertEquals(SCOPE_ID_WITH_DOMAIN, scopeCaptor.getValue().getId());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetScope() {
        Scope expectedScope = new Scope(SCOPE_ID, null, null, null, null, null);

        when(scopeService.getScope(SCOPE_ID_WITH_DOMAIN)).thenReturn(expectedScope);

        Scope scope = RULE.client().target("/v1.0/" + DOMAIN_ID + "/scope/" + SCOPE_ID_WITH_DOMAIN)
                        .request(MediaType.APPLICATION_JSON_TYPE).get(Scope.class);

        verify(scopeService).getScope(eq(SCOPE_ID_WITH_DOMAIN));
        assertEquals(scope, expectedScope);
    }

    @Test
    public void testGetUnknownScope() {
        when(scopeService.getScope(SCOPE_ID_WITH_DOMAIN)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/scope/" + SCOPE_ID_WITH_DOMAIN)
                            .request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

        verify(scopeService).getScope(eq(SCOPE_ID_WITH_DOMAIN));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteScope() {
        Response response = RULE.client().target("/v1.0/" + DOMAIN_ID + "/scope/" + SCOPE_ID_WITH_DOMAIN)
                            .request().delete(Response.class);
        verify(scopeService).delete(eq(SCOPE_ID_WITH_DOMAIN));
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}