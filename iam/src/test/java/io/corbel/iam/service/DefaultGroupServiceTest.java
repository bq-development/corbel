package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.exception.NotExistentScopeException;
import io.corbel.iam.model.Group;
import io.corbel.iam.model.Scope;
import io.corbel.iam.repository.GroupRepository;
import io.corbel.iam.repository.ScopeRepository;
import io.corbel.lib.queries.request.ResourceQuery;

@RunWith(MockitoJUnitRunner.class) public class DefaultGroupServiceTest {

    private static final String ID = "id";
    private static final String NEW_ID = "newId";
    private static final String NAME = "name";
    private static final String DOMAIN = "domain";

    public static final String SCOPE_1 = "scope1";
    public static final String SCOPE_2 = "scope2";
    public static final String SCOPE_3 = "scope3";

    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList(SCOPE_1, SCOPE_2));

    @Mock private GroupRepository groupRepositoryMock;
    @Mock
    private ScopeRepository scopeRepositoryMock;

    private GroupService groupService;

    @Before
    public void setUp() {
        groupService = new DefaultGroupService(groupRepositoryMock, scopeRepositoryMock);
    }

    @Test
    public void getAllGroupsTest() {
        Group group = getGroup();

        List<ResourceQuery> resourceQueries = Collections.emptyList();

        when(groupRepositoryMock.findByDomain(DOMAIN, resourceQueries, null, null)).thenReturn(Collections.singletonList(group));

        List<Group> groups = groupService.getAll(DOMAIN, resourceQueries, null, null);

        assertThat(groups).hasSize(1);
        assertThat(groups).contains(group);

        verify(groupRepositoryMock).findByDomain(DOMAIN, resourceQueries, null, null);
        verifyNoMoreInteractions(groupRepositoryMock);
    }

    @Test
    public void getGroupTest() {
        Group expectedGroup = getGroup();

        when(groupRepositoryMock.findByIdAndDomain(ID, DOMAIN)).thenReturn(expectedGroup);

        Optional<Group> group = groupService.getById(ID, DOMAIN);

        assertThat(group.isPresent()).isTrue();

        assertThat(group.get()).isEqualTo(expectedGroup);

        verify(groupRepositoryMock).findByIdAndDomain(ID, DOMAIN);
        verifyNoMoreInteractions(groupRepositoryMock);
    }

    @Test
    public void getNullGroupTest() {
        Optional<Group> group = groupService.getById(ID, DOMAIN);

        assertThat(group.isPresent()).isFalse();

        verify(groupRepositoryMock).findByIdAndDomain(ID, DOMAIN);
        verifyNoMoreInteractions(groupRepositoryMock);
    }

    @Test
    public void getGroupWithoutDomainTest() {
        Group expectedGroup = getGroup();

        when(groupRepositoryMock.findOne(ID)).thenReturn(expectedGroup);

        Optional<Group> group = groupService.getById(ID);

        assertThat(group.isPresent()).isTrue();

        assertThat(group.get()).isEqualTo(expectedGroup);

        verify(groupRepositoryMock).findOne(ID);
        verifyNoMoreInteractions(groupRepositoryMock);
    }

    @Test
    public void getNullWithoutDomainGroupTest() {
        Optional<Group> group = groupService.getById(ID);

        assertThat(group.isPresent()).isFalse();

        verify(groupRepositoryMock).findOne(ID);
        verifyNoMoreInteractions(groupRepositoryMock);
    }

    @Test
    public void createGroupTest() throws GroupAlreadyExistsException, NotExistentScopeException {
        Group group = getGroup();

        when(scopeRepositoryMock.findOne(SCOPE_1)).thenReturn(mock(Scope.class));
        when(scopeRepositoryMock.findOne(SCOPE_2)).thenReturn(mock(Scope.class));
        groupService.create(group);

        ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);

        verify(groupRepositoryMock).insert(capturedGroup.capture());
        verify(scopeRepositoryMock).findOne(SCOPE_1);
        verify(scopeRepositoryMock).findOne(SCOPE_2);

        Group savedGroup = capturedGroup.getValue();

        assertThat(savedGroup.getId()).isNull();
        assertThat(savedGroup.getName()).isEqualTo(group.getName());
        assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
        assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

        verifyNoMoreInteractions(groupRepositoryMock, scopeRepositoryMock);
    }

    @Test
    public void createGroupWithoutScopesTest() throws GroupAlreadyExistsException, NotExistentScopeException {
        Group group = getGroup();
        group.setScopes(null);
        groupService.create(group);

        ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);
        verify(groupRepositoryMock).insert(capturedGroup.capture());

        Group savedGroup = capturedGroup.getValue();

        assertThat(savedGroup.getId()).isNull();
        assertThat(savedGroup.getName()).isEqualTo(group.getName());
        assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
        assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

        verifyNoMoreInteractions(groupRepositoryMock, scopeRepositoryMock);
    }

    @Test(expected = GroupAlreadyExistsException.class)
    public void createAlreadyExistentGroupTest() throws GroupAlreadyExistsException, NotExistentScopeException {
        Group group = getGroup();

        when(scopeRepositoryMock.findOne(any())).thenReturn(mock(Scope.class));
        doThrow(new DataIntegrityViolationException(NEW_ID)).when(groupRepositoryMock).insert(Mockito.<Group>any());

        try {
            groupService.create(group);
        } catch (GroupAlreadyExistsException e) {

            ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);

            verify(groupRepositoryMock).insert(capturedGroup.capture());

            Group savedGroup = capturedGroup.getValue();

            assertThat(savedGroup.getId()).isNull();
            assertThat(savedGroup.getName()).isEqualTo(group.getName());
            assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
            assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

            verifyNoMoreInteractions(groupRepositoryMock);

            throw e;
        }
    }

    @Test
    public void deleteGroupTest() {
        groupService.delete(ID, DOMAIN);

        verify(groupRepositoryMock).deleteByIdAndDomain(ID, DOMAIN);
        verifyNoMoreInteractions(groupRepositoryMock);
    }

    @Test
    public void addScopesToGroupTest() throws NotExistentScopeException {
        String[] scopes = {"newScope"};

        when(scopeRepositoryMock.findOne("newScope")).thenReturn(mock(Scope.class));
        groupService.addScopes(ID, scopes);

        verify(groupRepositoryMock).addScopes(eq(ID), any());
        verify(scopeRepositoryMock).findOne("newScope");

        verifyNoMoreInteractions(groupRepositoryMock, scopeRepositoryMock);
    }

    @Test(expected = NotExistentScopeException.class)
    public void addNotExistenteScopesToGroupTest() throws NotExistentScopeException {
        String[] scopes = {"newScope"};
        when(scopeRepositoryMock.findOne("newScope")).thenReturn(null);
        groupService.addScopes(ID, scopes);
    }

    @Test
    public void removeScopesFromGroupTest() {
        String[] scopes = {"scopeToRemove"};

        groupService.removeScopes(ID, scopes);

        verify(groupRepositoryMock).removeScopes(eq(ID), any());
        verifyNoMoreInteractions(groupRepositoryMock);
    }


    @Test
    public void testGroupScopes() {
        List<String> groups = Arrays.asList("Admins", "Users");
        Group administrators = new Group("Admins", "Admins", "MyDomain", new HashSet<>(Arrays.asList(SCOPE_1)));
        Group users = new Group("Admins", "Users", "MyDomain", new HashSet<>(Arrays.asList(SCOPE_2, SCOPE_3)));

        when(groupRepositoryMock.findByNameAndDomain("Admins", DOMAIN)).thenReturn(administrators);
        when(groupRepositoryMock.findByNameAndDomain("Users", DOMAIN)).thenReturn(users);

        Set<String> scopes = groupService.getGroupScopes(DOMAIN, groups);

        assertThat(scopes).contains(SCOPE_1);
        assertThat(scopes).contains(SCOPE_2);
        assertThat(scopes).contains(SCOPE_3);
    }

    private Group getGroup() {
        return new Group(ID, NAME, DOMAIN, SCOPES);
    }
}
