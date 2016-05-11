package io.corbel.iam.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.exception.NotExistentScopeException;
import io.corbel.iam.model.Group;
import io.corbel.iam.repository.GroupRepository;
import io.corbel.iam.repository.ScopeRepository;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import org.springframework.util.CollectionUtils;

public class DefaultGroupService implements GroupService {

    private final GroupRepository groupRepository;
    private final ScopeRepository scopeRepository;

    public DefaultGroupService(GroupRepository groupRepository, ScopeRepository scopeRepository) {
        this.groupRepository = groupRepository;
        this.scopeRepository = scopeRepository;
    }

    @Override
    public Optional<Group> getById(String id) {
        return Optional.ofNullable(groupRepository.findOne(id));
    }

    @Override
    public Optional<Group> getById(String id, String domain) {
        return Optional.ofNullable(groupRepository.findByIdAndDomain(id, domain));
    }

    @Override
    public List<Group> getAll(String domain, List<ResourceQuery> resourceQueries, Pagination pagination, Sort sort) {
        return groupRepository.findByDomain(domain, resourceQueries, pagination, sort);
    }

    @Override
    public Set<String> getGroupScopes(String domain, Collection<String> groups) {
        Set<String> scopes = new HashSet<>();
        groups.stream().forEach(
                groupName -> Optional.ofNullable(groupRepository.findByNameAndDomain(groupName, domain)).ifPresent(group -> scopes.addAll(group.getScopes())));
        return scopes;
    }


    @Override
    public Group create(Group group) throws GroupAlreadyExistsException, NotExistentScopeException {
        group.setId(null);
        try {
            checkScopes(group.getScopes());
            groupRepository.insert(group);
            return group;
        } catch (DataIntegrityViolationException e) {
            throw new GroupAlreadyExistsException(group.getName() + " in domain " + group.getDomain());
        }
    }

    @Override
    public void addScopes(String id, String... scopes) throws NotExistentScopeException {
        checkScopes(Arrays.asList(scopes));
        groupRepository.addScopes(id, scopes);
    }

    @Override
    public void removeScopes(String id, String... scopes) {
        groupRepository.removeScopes(id, scopes);
    }

    @Override
    public void delete(String id, String domain) {
        groupRepository.deleteByIdAndDomain(id, domain);
    }

    private void checkScopes(Collection<String> scopes) throws NotExistentScopeException {
        if (CollectionUtils.isEmpty(scopes)) {
            return;
        }

        String notExistentScopes = scopes.stream().filter(scope -> scopeRepository.findOne(scope.split(DefaultScopeService.SCOPE_PARAMS_SEPARATOR)[DefaultScopeService.SCOPE_ID_POSITION]) == null)
                .collect(Collectors.joining(", "));

        if (!notExistentScopes.isEmpty()) {
            throw new NotExistentScopeException(notExistentScopes);
        }
    }

}
