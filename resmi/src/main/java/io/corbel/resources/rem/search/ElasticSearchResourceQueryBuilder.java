package io.corbel.resources.rem.search;

import io.corbel.lib.queries.request.QueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rubén Carrasco
 */
public class ElasticSearchResourceQueryBuilder {

    public static QueryBuilder build(String search, ResourceQuery query) {
        return build(search, query != null ? Collections.singletonList(query) : Collections.emptyList());
    }

    public static QueryBuilder build(String search, List<ResourceQuery> queries) {
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(search);
        if (queries == null || queries.isEmpty()) {
            return queryStringQueryBuilder;
        } else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (ResourceQuery query : queries) {
                boolQueryBuilder.should(getBoolQueryBuilder(query, queryStringQueryBuilder));
            }
            return boolQueryBuilder;
        }
    }

    private static QueryBuilder getBoolQueryBuilder(ResourceQuery query, QueryStringQueryBuilder queryStringQueryBuilder) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(queryStringQueryBuilder);
        for (QueryNode node : query) {
            if (isNegativeQuery(node)) {
                boolQueryBuilder.mustNot(getFilterBuilder(node));
            } else {
                boolQueryBuilder.filter(getFilterBuilder(node));
            }
        }
        return boolQueryBuilder;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static QueryBuilder getFilterBuilder(QueryNode node) {
        switch (node.getOperator()) {
            case $NE:
            case $EQ:
                return QueryBuilders.termQuery(node.getField(), node.getValue().getLiteral());
            case $GT:
                return QueryBuilders.rangeQuery(node.getField()).gt(node.getValue().getLiteral());
            case $GTE:
                return QueryBuilders.rangeQuery(node.getField()).gte(node.getValue().getLiteral());
            case $LT:
                return QueryBuilders.rangeQuery(node.getField()).lt(node.getValue().getLiteral());
            case $LTE:
                return QueryBuilders.rangeQuery(node.getField()).lte(node.getValue().getLiteral());
            case $EXISTS:
                return QueryBuilders.existsQuery(node.getField());
            case $IN:
            case $NIN:
                return QueryBuilders.termsQuery(node.getField(), getValues((List<QueryLiteral>) node.getValue().getLiteral()));
            default:
                throw new ElasticsearchException("Given filter cannot be applied");
        }
    }

    private static boolean isNegativeQuery(QueryNode node) {
        return node.getOperator() == QueryOperator.$NE ||
                node.getOperator() == QueryOperator.$NIN ||
                (node.getOperator() == QueryOperator.$EXISTS && !(Boolean) node.getValue().getLiteral());
    }

    private static Object[] getValues(@SuppressWarnings("rawtypes") List<QueryLiteral> literals) {
        return literals.stream().map(QueryLiteral::getLiteral).collect(Collectors.toList()).toArray();
    }
}
