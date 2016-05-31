package io.corbel.resources.rem.acl.query;

import io.corbel.lib.queries.ListQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.resources.rem.model.AclPermission;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Rubén Carrasco
 */
public class AclQueryBuilderTest {

    private static final String FIELD = "field";
    private static final String LITERAL = "literal";
    private static final String _ACL_GROUP_GROUP2 = "_acl.group:group2.permission";
    private static final String _ACL_GROUP_GROUP1 = "_acl.group:group1.permission";
    private static final String GROUP2 = "group2";
    private static final String GROUP1 = "group1";
    private static final List<String> GROUPS = Arrays.asList(GROUP1, GROUP2);
    private static final String _ACL_USER_USER_ID = "_acl.user:userId.permission";
    private static final String _ACL_ALL = "_acl.ALL.permission";
    private static final String USER_ID = "userId";
    private static final Optional<String> OPT_USER_ID = Optional.of(USER_ID);

    @Test
    public void testWithUserId() {
        AclQueryBuilder builder = new AclQueryBuilder(OPT_USER_ID, Collections.emptyList());
        List<ResourceQuery> queries = builder.build(Collections.emptyList());
        assertThat(queries.contains(getResourceQuery(getListQueryLiteral(_ACL_ALL)))).isTrue();
        assertThat(queries.contains(getResourceQuery(getListQueryLiteral(_ACL_USER_USER_ID)))).isTrue();
    }

    @Test
    public void testWithGroups() {
        AclQueryBuilder builder = new AclQueryBuilder(Optional.empty(), GROUPS);
        List<ResourceQuery> queries = builder.build(Collections.emptyList());
        assertThat(queries.contains(getResourceQuery(getListQueryLiteral(_ACL_ALL)))).isTrue();
        assertThat(queries.contains(getResourceQuery(getListQueryLiteral(_ACL_GROUP_GROUP1)))).isTrue();
        assertThat(queries.contains(getResourceQuery(getListQueryLiteral(_ACL_GROUP_GROUP2)))).isTrue();
    }

    @Test
    public void testWithQueries() {
        List<QueryNode> nodes = Arrays.asList(
                getListQueryLiteral(_ACL_ALL),
                getListQueryLiteral(_ACL_USER_USER_ID),
                getListQueryLiteral(_ACL_GROUP_GROUP1),
                getListQueryLiteral(_ACL_GROUP_GROUP2),
                getTestQueryLiteral());

        AclQueryBuilder builder = new AclQueryBuilder(OPT_USER_ID, GROUPS);
        List<ResourceQuery> queries = builder
                .build(Arrays.asList(getResourceQuery(getTestQueryLiteral()), getResourceQuery(getTestQueryLiteral())));
        queries.forEach(query -> {
            assertThat(query.getFilters()).hasSize(2);
            query.forEach(queryNode -> assertThat(nodes.contains(queryNode)).isTrue());
        });
    }

    private ResourceQuery getResourceQuery(QueryNode node) {
        ResourceQuery query = new ResourceQuery();
        query.addQueryNode(node);
        return query;
    }

    private QueryNodeImpl getListQueryLiteral(String field) {
        return new QueryNodeImpl(QueryOperator.$IN, field, new ListQueryLiteral(Arrays.asList(
                new StringQueryLiteral(AclPermission.READ.name()),
                new StringQueryLiteral(AclPermission.WRITE.name()),
                new StringQueryLiteral(AclPermission.ADMIN.name()))));
    }

    private QueryNodeImpl getTestQueryLiteral() {
        StringQueryLiteral queryLiteral = new StringQueryLiteral(LITERAL);
        return new QueryNodeImpl(QueryOperator.$EQ, FIELD, queryLiteral);
    }

}
