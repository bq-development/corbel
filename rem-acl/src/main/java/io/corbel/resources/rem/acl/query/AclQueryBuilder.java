package io.corbel.resources.rem.acl.query;

import java.util.*;
import java.util.stream.Collectors;

import io.corbel.lib.queries.BooleanQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.resources.rem.service.DefaultAclResourcesService;

/**
 * @author Rubén Carrasco
 *
 */
public class AclQueryBuilder {

    List<QueryNode> existsQueryNodes;

    public AclQueryBuilder(Optional<String> userId, Collection<String> groupIds) {
        existsQueryNodes = Optional.ofNullable(groupIds).orElseGet(Collections::emptyList).stream()
                .map(groupId -> buildQueryNodeExistInAcl(DefaultAclResourcesService.GROUP_PREFIX + groupId)).collect(Collectors.toList());
        existsQueryNodes.add(buildQueryNodeExistInAcl(DefaultAclResourcesService.ALL));
        userId.ifPresent(id -> existsQueryNodes.add(buildQueryNodeExistInAcl(DefaultAclResourcesService.USER_PREFIX + id)));
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

    private static QueryNodeImpl buildQueryNodeExistInAcl(String id) {
        BooleanQueryLiteral booleanQueryLiteral = new BooleanQueryLiteral();
        booleanQueryLiteral.setLiteral(true);
        return new QueryNodeImpl(QueryOperator.$EXISTS, DefaultAclResourcesService._ACL + "." + id, booleanQueryLiteral);
    }

}
