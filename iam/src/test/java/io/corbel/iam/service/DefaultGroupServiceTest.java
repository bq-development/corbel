package io.corbel.iam.service;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.model.Group;
import io.corbel.iam.repository.GroupRepository;
import io.corbel.lib.queries.request.ResourceQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class) public class DefaultGroupServiceTest {

    private static final String ID = "id";
    private static final String NEW_ID = "newId";
    private static final String NAME = "name";
    private static final String DOMAIN = "domain";
    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList("scope1", "scope2"));

    @Mock private GroupRepository groupRepository;

    private GroupService groupService;

    @Before
    public void setUp() {
        groupService = new DefaultGroupService(groupRepository);
    }

    @Test
    public void getAllGroupsTest() {
        Group group = getGroup();

        List<ResourceQuery> resourceQueries = Collections.emptyList();

        when(groupRepository.findByDomain(DOMAIN, resourceQueries, null, null)).thenReturn(Collections.singletonList(group));

        List<Group> groups = groupService.getAll(DOMAIN, resourceQueries, null, null);

        assertThat(groups).hasSize(1);
        assertThat(groups).contains(group);

        verify(groupRepository).findByDomain(DOMAIN, resourceQueries, null, null);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void getGroupTest() {
        Group expectedGroup = getGroup();

        when(groupRepository.findByIdAndDomain(ID, DOMAIN)).thenReturn(expectedGroup);

        Optional<Group> group = groupService.get(ID, DOMAIN);

        assertThat(group.isPresent()).isTrue();

        assertThat(group.get()).isEqualTo(expectedGroup);

        verify(groupRepository).findByIdAndDomain(ID, DOMAIN);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void getNullGroupTest() {
        Optional<Group> group = groupService.get(ID, DOMAIN);

        assertThat(group.isPresent()).isFalse();

        verify(groupRepository).findByIdAndDomain(ID, DOMAIN);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void getGroupWithoutDomainTest() {
        Group expectedGroup = getGroup();

        when(groupRepository.findOne(ID)).thenReturn(expectedGroup);

        Optional<Group> group = groupService.get(ID);

        assertThat(group.isPresent()).isTrue();

        assertThat(group.get()).isEqualTo(expectedGroup);

        verify(groupRepository).findOne(ID);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void getNullWithoutDomainGroupTest() {
        Optional<Group> group = groupService.get(ID);

        assertThat(group.isPresent()).isFalse();

        verify(groupRepository).findOne(ID);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void createGroupTest() throws GroupAlreadyExistsException {
        Group group = getGroup();

        groupService.create(group);

        ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);

        verify(groupRepository).insert(capturedGroup.capture());

        Group savedGroup = capturedGroup.getValue();

        assertThat(savedGroup.getId()).isNull();
        assertThat(savedGroup.getName()).isEqualTo(group.getName());
        assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
        assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

        verifyNoMoreInteractions(groupRepository);
    }

    @Test(expected = GroupAlreadyExistsException.class)
    public void createAlreadyExistentGroupTest() throws GroupAlreadyExistsException {
        Group group = getGroup();

        doThrow( new DataIntegrityViolationException(NEW_ID)).when(groupRepository).insert(Mockito.<Group>any());

        try {
            groupService.create(group);

        } catch (GroupAlreadyExistsException e) {

            ArgumentCaptor<Group> capturedGroup = ArgumentCaptor.forClass(Group.class);

            verify(groupRepository).insert(capturedGroup.capture());

            Group savedGroup = capturedGroup.getValue();

            assertThat(savedGroup.getId()).isNull();
            assertThat(savedGroup.getName()).isEqualTo(group.getName());
            assertThat(savedGroup.getDomain()).isEqualTo(group.getDomain());
            assertThat(savedGroup.getScopes()).isEqualTo(group.getScopes());

            verifyNoMoreInteractions(groupRepository);

            throw e;
        }
    }

    @Test
    public void deleteGroupTest() {
        groupService.delete(ID, DOMAIN);

        verify(groupRepository).deleteByIdAndDomain(ID, DOMAIN);
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void addScopesToGroupTest() {
        String[] scopes = {"newScope"};

        groupService.addScopes(ID, scopes);

        verify(groupRepository).addScopes(eq(ID), any());
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void removeScopesFromGroupTest() {
        String[] scopes = {"scopeToRemove"};

        groupService.removeScopes(ID, scopes);

        verify(groupRepository).removeScopes(eq(ID), any());
        verifyNoMoreInteractions(groupRepository);
    }

    private Group getGroup() {
        return new Group(ID, NAME, DOMAIN, SCOPES);
    }
}
