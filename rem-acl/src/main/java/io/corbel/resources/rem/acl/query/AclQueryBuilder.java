package io.corbel.resources.rem.acl.query;

import io.corbel.lib.queries.ListQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.resources.rem.model.AclPermission;
import io.corbel.resources.rem.service.DefaultAclResourcesService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rubén Carrasco
 */
public class AclQueryBuilder {

    List<QueryNode> existsQueryNodes;

    public AclQueryBuilder(Optional<String> userId, Collection<String> groupIds) {
        existsQueryNodes = Optional.ofNullable(groupIds).orElseGet(Collections::emptyList).stream()
                .map(groupId -> buildPermissionsInAclQueryNode(DefaultAclResourcesService.GROUP_PREFIX + groupId)).collect(Collectors.toList());
        existsQueryNodes.add(buildPermissionsInAclQueryNode(DefaultAclResourcesService.ALL));
        userId.ifPresent(id -> existsQueryNodes.add(buildPermissionsInAclQueryNode(DefaultAclResourcesService.USER_PREFIX + id)));
    }

    public List<ResourceQuery> build(List<ResourceQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            return buildDefaultAclQueries();
        }

        List<ResourceQuery> aclQueryParams = new LinkedList<>();
        queries.stream().forEach(query -> existsQueryNodes.forEach(existsQueryNode -> {
            ResourceQuery newQuery = query.clone();
            newQuery.addQueryNode(existsQueryNode);
            aclQueryParams.add(newQuery);
        }));
        return aclQueryParams;
    }

    private List<ResourceQuery> buildDefaultAclQueries() {
        return existsQueryNodes.stream().map(existsQueryNode -> {
            ResourceQuery query = new ResourceQuery();
            query.addQueryNode(existsQueryNode);
            return query;
        }).collect(Collectors.toList());
    }

    private static QueryNodeImpl buildPermissionsInAclQueryNode(String id) {
        return new QueryNodeImpl(QueryOperator.$IN, DefaultAclResourcesService._ACL + "." + id + ".permission",
                new ListQueryLiteral(Arrays.asList(new StringQueryLiteral(AclPermission.READ.name()),
                        new StringQueryLiteral(AclPermission.WRITE.name()), new StringQueryLiteral(AclPermission.ADMIN.name()))));
    }

}
