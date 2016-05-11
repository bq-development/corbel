package io.corbel.iam.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.exception.NotExistentScopeException;
import io.corbel.iam.model.Group;
import io.corbel.iam.service.GroupService;
import io.corbel.lib.queries.parser.*;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.api.error.JsonValidationExceptionMapper;
import io.corbel.lib.ws.auth.*;
import io.corbel.lib.ws.queries.QueryParametersProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

public class GroupResourceTest {

    private static final GroupService groupService = mock(GroupService.class);

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DOMAIN = "domain";
    private static final String ANOTHER_DOMAIN = "anotherDomain";
    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList("scope1", "scope2"));
    private static final String TOKEN = "token";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final String SCOPES_PATH = "/scope";

    public static final QueryParser queryParserMock = mock(QueryParser.class);
    public static final AggregationParser aggregationParserMock = mock(AggregationParser.class);
    public static final SortParser sortParserMock = mock(SortParser.class);
    public static final SearchParser searchParserMock = mock(SearchParser.class);
    public static final PaginationParser paginationParserMock = mock(PaginationParser.class);

    public static final QueryParametersParser queryParametersParser = new QueryParametersParser(queryParserMock, aggregationParserMock,
            sortParserMock, paginationParserMock, searchParserMock);

    private static final BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static final AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);


    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    @SuppressWarnings("unchecked") private static CookieOAuthFactory<AuthorizationInfo> cookieOAuthFactory = mock(CookieOAuthFactory.class);
    @SuppressWarnings("unchecked") private static final AuthorizationRequestFilter filter = spy(
            new AuthorizationRequestFilter(oAuthFactory, cookieOAuthFactory, "", false, "group"));

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addResource(new GroupResource(groupService))
            .addProvider(filter).addProvider(new AuthorizationInfoProvider().getBinder()).addProvider(GenericExceptionMapper.class)
            .addProvider(JsonValidationExceptionMapper.class)
            .addProvider(new QueryParametersProvider(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE, queryParametersParser).getBinder()).build();

    @Before
    public void setUp() throws AuthenticationException {
        reset(authorizationInfoMock, authenticatorMock, cookieOAuthFactory, groupService);

        when(cookieOAuthFactory.provide()).thenReturn(authorizationInfoMock);
        when(cookieOAuthFactory.clone(false)).thenReturn(cookieOAuthFactory);
        when(authenticatorMock.authenticate(TOKEN)).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getDomainId()).thenReturn(DOMAIN);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
    }

    @Test
    public void createGroupTest() throws JsonProcessingException, GroupAlreadyExistsException, NotExistentScopeException {
        Group group = new Group(null, NAME, DOMAIN, SCOPES);
        Group createdGroup = new Group(ID, NAME, DOMAIN, SCOPES);

        when(groupService.create(eq(group))).thenReturn(createdGroup);

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group").request().post(Entity.json(groupJson),
                Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(((String) response.getHeaders().getFirst("Location")).endsWith(createdGroup.getId())).isTrue();
    }


    @Test
    public void createGroupWithBadScopesTest() throws JsonProcessingException, GroupAlreadyExistsException, NotExistentScopeException {
        Group group = new Group(null, NAME, DOMAIN, SCOPES);

        when(groupService.create(eq(group))).thenThrow(NotExistentScopeException.class);

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group").request().post(Entity.json(groupJson),
                Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void createAlreadyExistingGroupTest() throws GroupAlreadyExistsException, JsonProcessingException, NotExistentScopeException {
        Group group = new Group(null, NAME, DOMAIN, SCOPES);

        when(groupService.create(eq(group))).thenThrow(new GroupAlreadyExistsException(NAME + " " + ID));

        String groupJson = new ObjectMapper().writer().writeValueAsString(group);

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group").request().post(Entity.json(groupJson),
                Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getAllGroupsTest() {
        when(groupService.getAll(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group").request().get(List.class)).hasSize(0);
    }

    @Test
    public void getGroupTest() {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);

        when(groupService.getById(eq(ID), eq(DOMAIN))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID).request().get(Group.class)).isEqualTo(group);
    }

    @Test
    public void getNonexistentGroupTest() {
        when(groupService.getById(eq(ID), eq(DOMAIN))).thenReturn(Optional.empty());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID).request().get(Response.class).getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteGroupTest() {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID).request().delete().getStatus())
                .isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(groupService).getById(eq(ID));
        verify(groupService).delete(eq(ID), eq(DOMAIN));
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void deleteUnauthorizedGroupTest() {
        Group group = new Group(ID, NAME, ANOTHER_DOMAIN, SCOPES);

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID).request().delete().getStatus())
                .isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());

        verify(groupService).getById(eq(ID));
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void deleteNonExistentGroupTest() {
        when(groupService.getById(eq(ID))).thenReturn(Optional.empty());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID).request().delete().getStatus())
                .isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(groupService).getById(eq(ID));
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void addScopesToGroupTest() throws JsonProcessingException, NotExistentScopeException {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);
        List<String> scopes = Collections.singletonList("scope");
        String scopesToJson = new ObjectMapper().writer().writeValueAsString(scopes);

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH).request()
                .put(Entity.json(scopesToJson)).getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(groupService).getById(eq(ID));
        verify(groupService).addScopes(eq(ID), any());
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void addInexistentScopesToGroupTest() throws JsonProcessingException, NotExistentScopeException {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);
        List<String> scopes = Collections.singletonList("scope");
        String scopesToJson = new ObjectMapper().writer().writeValueAsString(scopes);

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));
        doThrow(NotExistentScopeException.class).when(groupService).addScopes(ID, "scope");

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH).request()
                .put(Entity.json(scopesToJson));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        verify(groupService).getById(eq(ID));
        verify(groupService).addScopes(ID, "scope");
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void addScopesToInexistentGroupTest() throws JsonProcessingException {
        List<String> scopes = Collections.singletonList("scope");
        String scopesToJson = new ObjectMapper().writer().writeValueAsString(scopes);

        when(groupService.getById(eq(ID))).thenReturn(Optional.empty());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH).request().put(Entity.json(scopesToJson))
                .getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

        verify(groupService).getById(eq(ID));
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void addScopesToUnauthorizedGroupTest() throws JsonProcessingException {
        Group group = new Group(ID, NAME, ANOTHER_DOMAIN, SCOPES);
        List<String> scopes = Collections.singletonList("scope");
        String scopesToJson = new ObjectMapper().writer().writeValueAsString(scopes);

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH).request().put(Entity.json(scopesToJson))
                .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());

        verify(groupService).getById(eq(ID));
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void removeScopesFromGroupTest() throws JsonProcessingException {
        Group group = new Group(ID, NAME, DOMAIN, SCOPES);
        String scope = "scope";

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH + "/" + scope).request().delete().getStatus())
                .isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(groupService).getById(eq(ID));
        verify(groupService).removeScopes(eq(ID), any());
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void removeScopesFromInexistentGroupTest() throws JsonProcessingException {
        String scope = "scope";

        when(groupService.getById(eq(ID))).thenReturn(Optional.empty());

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH + "/" + scope).request().delete().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

        verify(groupService).getById(eq(ID));
        verifyNoMoreInteractions(groupService);
    }

    @Test
    public void removeScopesFromUnauthorizedGroupTest() throws JsonProcessingException {
        Group group = new Group(ID, NAME, ANOTHER_DOMAIN, SCOPES);
        String scope = "scope";

        when(groupService.getById(eq(ID))).thenReturn(Optional.of(group));

        assertThat(RULE.client().target("/" + ApiVersion.CURRENT + "/" + DOMAIN + "/group/" + ID + SCOPES_PATH + "/" + scope).request().delete().getStatus())
                .isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());

        verify(groupService).getById(eq(ID));
        verifyNoMoreInteractions(groupService);
    }

}
