package io.corbel.resources.rem.search;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.*;

import io.corbel.lib.queries.request.QueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.ResourceQuery;

/**
 * @author Rubén Carrasco
 *
 */
public class ElasticSearchResourceQueryBuilder {

    public static QueryBuilder build(String search, ResourceQuery query) {
        return build(search, query != null ? Collections.singletonList(query) : Collections.emptyList());
    }

    public static QueryBuilder build(String search, List<ResourceQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            return QueryBuilders.queryStringQuery(search);
        } else {
            OrFilterBuilder orBuilder = FilterBuilders.orFilter();
            for (ResourceQuery query : queries) {
                orBuilder.add(getFilterBuilder(query));
            }
            return QueryBuilders.filteredQuery(QueryBuilders.queryStringQuery(search), orBuilder);
        }
    }

    private static FilterBuilder getFilterBuilder(ResourceQuery query) {
        AndFilterBuilder andFilterBuilder = FilterBuilders.andFilter();
        for (QueryNode node : query) {
            andFilterBuilder.add(getFilterBuilder(node));
        }
        return andFilterBuilder;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static FilterBuilder getFilterBuilder(QueryNode node) {
        switch (node.getOperator()) {
            case $EQ:
                return FilterBuilders.termFilter(node.getField(), node.getValue().getLiteral());
            case $NE:
                return FilterBuilders.notFilter(FilterBuilders.termFilter(node.getField(), node.getValue().getLiteral()));
            case $GT:
                return FilterBuilders.rangeFilter(node.getField()).gt(node.getValue().getLiteral());
            case $GTE:
                return FilterBuilders.rangeFilter(node.getField()).gte(node.getValue().getLiteral());
            case $LT:
                return FilterBuilders.rangeFilter(node.getField()).lt(node.getValue().getLiteral());
            case $LTE:
                return FilterBuilders.rangeFilter(node.getField()).lte(node.getValue().getLiteral());
            case $EXISTS:
                return (Boolean) node.getValue().getLiteral() ? FilterBuilders.existsFilter(node.getField())
                        : FilterBuilders.notFilter(FilterBuilders.existsFilter(node.getField()));
            case $IN:
                return FilterBuilders.inFilter(node.getField(), getValues((List<QueryLiteral>) node.getValue().getLiteral()));
            case $NIN:
                return FilterBuilders
                        .notFilter(FilterBuilders.inFilter(node.getField(), getValues((List<QueryLiteral>) node.getValue().getLiteral())));
            default:
                return null;
        }
    }

    private static Object[] getValues(@SuppressWarnings("rawtypes") List<QueryLiteral> literals) {
        return literals.stream().map(QueryLiteral::getLiteral).collect(Collectors.toList()).toArray();
    }
}
