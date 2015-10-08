package io.corbel.resources.rem.request.builder;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.lib.queries.request.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Rubén Carrasco
 *
 */
public class QueryParametersBuilder {

    private Pagination pagination;
    private Sort sort;
    private List<ResourceQuery> queries;
    private List<ResourceQuery> conditions;
    private Aggregation aggregation;
    private Search search;

    public QueryParametersBuilder() {}

    public QueryParametersBuilder(QueryParameters queryParameters) {
        this.pagination = queryParameters.getPagination();
        this.sort = queryParameters.getSort().get();
        this.queries = queryParameters.getQueries().get();
        this.conditions = queryParameters.getConditions().get();
        this.aggregation = queryParameters.getAggregation().get();
        this.search = queryParameters.getSearch().get();
    }

    public QueryParameters build() {
        return new QueryParameters(pagination, Optional.ofNullable(sort), Optional.ofNullable(queries), Optional.ofNullable(conditions),
                Optional.ofNullable(aggregation), Optional.ofNullable(search));
    }

    public QueryParametersBuilder pagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    public QueryParametersBuilder sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public QueryParametersBuilder queries(List<ResourceQuery> queries) {
        this.queries = queries;
        return this;
    }

    public QueryParametersBuilder queries(ResourceQuery... queries) {
        this.queries = Arrays.asList(queries);
        return this;
    }

    public QueryParametersBuilder query(ResourceQuery query) {
        if (queries == null) {
            queries = new ArrayList<>();
        }
        queries.add(query);
        return this;
    }

    public QueryParametersBuilder conditions(List<ResourceQuery> conditions) {
        this.conditions = conditions;
        return this;
    }

    public QueryParametersBuilder conditions(ResourceQuery... conditions) {
        this.conditions = Arrays.asList(conditions);
        return this;
    }

    public QueryParametersBuilder condition(ResourceQuery condition) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        conditions.add(condition);
        return this;
    }

    public QueryParametersBuilder aggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
        return this;
    }

    public QueryParametersBuilder search(Search search) {
        this.search = search;
        return this;
    }
}
