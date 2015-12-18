package io.corbel.resources.rem.acl.query;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.*;

import org.junit.Test;

import io.corbel.lib.queries.BooleanQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;

/**
 * @author Rubén Carrasco
 *
 */
public class AclQueryBuilderTest {

    private static final String FIELD = "field";
    private static final String LITERAL = "literal";
    private static final String _ACL_GROUP_GROUP2 = "_acl.group:group2";
    private static final String _ACL_GROUP_GROUP1 = "_acl.group:group1";
    private static final String GROUP2 = "group2";
    private static final String GROUP1 = "group1";
    private static final List<String> GROUPS = Arrays.asList(GROUP1, GROUP2);
    private static final String _ACL_USER_USER_ID = "_acl.user:userId";
    private static final String _ACL_ALL = "_acl.ALL";
    private static final String USER_ID = "userId";
    private static final Optional<String> OPT_USER_ID = Optional.of(USER_ID);

    @Test
    public void testWithUserId() {
        AclQueryBuilder builder = new AclQueryBuilder(OPT_USER_ID, Collections.emptyList());
        List<ResourceQuery> queries = builder.build(Collections.emptyList());
        assertThat(queries.contains(getResourceQuery(getBooleanQueryLiteral(_ACL_ALL)))).isTrue();
        assertThat(queries.contains(getResourceQuery(getBooleanQueryLiteral(_ACL_USER_USER_ID)))).isTrue();
    }

    @Test
    public void testWithGroups() {
        AclQueryBuilder builder = new AclQueryBuilder(Optional.empty(), GROUPS);
        List<ResourceQuery> queries = builder.build(Collections.emptyList());
        assertThat(queries.contains(getResourceQuery(getBooleanQueryLiteral(_ACL_ALL)))).isTrue();
        assertThat(queries.contains(getResourceQuery(getBooleanQueryLiteral(_ACL_GROUP_GROUP1)))).isTrue();
        assertThat(queries.contains(getResourceQuery(getBooleanQueryLiteral(_ACL_GROUP_GROUP2)))).isTrue();
    }

    @Test
    public void testWithQueries() {
        List<QueryNode> nodes = Arrays.asList(
            getBooleanQueryLiteral(_ACL_ALL),
            getBooleanQueryLiteral(_ACL_USER_USER_ID),
            getBooleanQueryLiteral(_ACL_GROUP_GROUP1),
            getBooleanQueryLiteral(_ACL_GROUP_GROUP2),
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

    private QueryNodeImpl getBooleanQueryLiteral(String field) {
        BooleanQueryLiteral booleanQueryLiteral = new BooleanQueryLiteral();
        booleanQueryLiteral.setLiteral(true);
        return new QueryNodeImpl(QueryOperator.$EXISTS, field, booleanQueryLiteral);
    }

    private QueryNodeImpl getTestQueryLiteral() {
        StringQueryLiteral queryLiteral = new StringQueryLiteral(LITERAL);
        return new QueryNodeImpl(QueryOperator.$EQ, FIELD, queryLiteral);
    }

}
