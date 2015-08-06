package io.corbel.iam.auth;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.oauth.jsontoken.JsonToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.model.Client;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.ClientRepository;
import io.corbel.iam.repository.DomainRepository;
import io.corbel.iam.repository.UserRepository;
import io.corbel.iam.service.ScopeService;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Alexander De Leon
 * 
 */
@RunWith(MockitoJUnitRunner.class) public class JsonTokenAuthorizationRequestContextTest {

    private static final String TEST_CLIENT_ID = "client";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_REQ_DOMAIN = "reqdomain";
    private static final String TEST_USER_ID = "user";
    private static final String TEST_SCOPES = "SCOPE_1 SCOPE_2 SCOPE_3";
    private static final String TEST_VERSION = "1.0.0";

    private static final String[] SCOPES_VALUES = new String[] {"a", "b"};
    private static final Set<String> SCOPES_SET = new HashSet<>(Arrays.asList(SCOPES_VALUES));

    @Mock private ClientRepository clientRepositoryMock;
    @Mock private DomainRepository domainRepositoryMock;
    @Mock private UserRepository userRepositoryMock;
    @Mock private JsonToken jsonTokenMock;
    @Mock private ScopeService scopeServiceMock;

    private JsonTokenAuthorizationRequestContext context;

    @Before
    public void setup() {
        context = new JsonTokenAuthorizationRequestContext(clientRepositoryMock, domainRepositoryMock, userRepositoryMock, jsonTokenMock);

        // jwt stub
        when(jsonTokenMock.getIssuer()).thenReturn(TEST_CLIENT_ID);

        // repository stubs
        Client client = new Client();
        client.setId(TEST_CLIENT_ID);
        client.setDomain(TEST_DOMAIN);
        client.setScopes(SCOPES_SET);
        when(clientRepositoryMock.findOne(TEST_CLIENT_ID)).thenReturn(client);

        Domain domain = new Domain();
        domain.setId(TEST_DOMAIN);
        when(domainRepositoryMock.findOne(TEST_DOMAIN)).thenReturn(domain);

        Domain reqDomain = new Domain();
        reqDomain.setId(TEST_REQ_DOMAIN);
        reqDomain.setDescription(TEST_REQ_DOMAIN);
        when(domainRepositoryMock.findOne(TEST_REQ_DOMAIN)).thenReturn(reqDomain);

        User user = new User();
        user.setId(TEST_USER_ID);
        when(userRepositoryMock.findByUsernameAndDomain(TEST_USER_ID, TEST_DOMAIN)).thenReturn(user);
    }

    @Test
    public void testGetClient() {
        assertThat(context.getIssuerClient().getId()).isEqualTo(TEST_CLIENT_ID);
    }

    @Test
    public void testGetDomain() {
        assertThat(context.getIssuerClientDomain().getId()).isEqualTo(TEST_DOMAIN);
    }

    @Test
    public void testGetPrincipal() {
        JsonObject json = new JsonObject();
        json.add("prn", new JsonPrimitive(TEST_USER_ID));
        when(jsonTokenMock.getPayloadAsJsonObject()).thenReturn(json);

        assertThat(context.hasPrincipal()).isTrue();
        assertThat(context.getPrincipal().getId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    public void testGetVersion() {
        JsonObject json = new JsonObject();
        json.add("version", new JsonPrimitive(TEST_VERSION));
        when(jsonTokenMock.getPayloadAsJsonObject()).thenReturn(json);

        assertThat(context.hasVersion()).isTrue();
        assertThat(context.getVersion()).isEqualTo(TEST_VERSION);
    }

    @Test
    public void testGetScopes() {
        JsonObject json = new JsonObject();
        json.add("scope", new JsonPrimitive(TEST_SCOPES));
        when(jsonTokenMock.getPayloadAsJsonObject()).thenReturn(json);

        assertThat(context.getRequestedScopes()).isEqualTo(new HashSet<>(Arrays.asList(TEST_SCOPES.split(" "))));
    }

    @Test
    public void testGetEmptyScopes() {
        when(jsonTokenMock.getPayloadAsJsonObject()).thenReturn(new JsonObject());
        assertThat(context.getRequestedScopes()).isEmpty();
    }

    @Test
    public void testGetRequestedDomain() {
        JsonObject json = new JsonObject();
        json.add("request_domain", new JsonPrimitive(TEST_REQ_DOMAIN));
        when(jsonTokenMock.getPayloadAsJsonObject()).thenReturn(json);
        assertThat(context.getRequestedDomain().getId()).isEqualTo(TEST_REQ_DOMAIN);
    }

    @Test
    public void testGetRequestedDomainEmpty() {
        when(jsonTokenMock.getPayloadAsJsonObject()).thenReturn(new JsonObject());
        assertThat(context.getRequestedDomain().getId()).isEqualTo(TEST_DOMAIN);
    }
}
