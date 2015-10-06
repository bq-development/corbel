package io.corbel.resources.rem.request.builder;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.lib.queries.request.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Rubén Carrasco
 *
 */
public class QueryParametersBuilder {

    private Pagination pagination;
    private Optional<Sort> sort = Optional.empty();
    private Optional<List<ResourceQuery>> queries = Optional.empty();
    private Optional<List<ResourceQuery>> conditions = Optional.empty();
    private Optional<Aggregation> aggregation = Optional.empty();
    private Optional<Search> search = Optional.empty();

    public QueryParameters build() {
        return new QueryParameters(pagination, sort, queries, conditions, aggregation, search);
    }

    public QueryParametersBuilder pagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    public QueryParametersBuilder sort(Sort sort) {
        this.sort = Optional.ofNullable(sort);
        return this;
    }

    public QueryParametersBuilder queries(List<ResourceQuery> queries) {
        this.queries = Optional.ofNullable(queries);
        return this;
    }

    public QueryParametersBuilder queries(ResourceQuery... queries) {
        this.queries = Optional.ofNullable(Arrays.asList(queries));
        return this;
    }

    public QueryParametersBuilder conditions(List<ResourceQuery> conditions) {
        this.conditions = Optional.ofNullable(conditions);
        return this;
    }

    public QueryParametersBuilder conditions(ResourceQuery... conditions) {
        this.conditions = Optional.ofNullable(Arrays.asList(conditions));
        return this;
    }

    public QueryParametersBuilder aggregation(Aggregation aggregation) {
        this.aggregation = Optional.ofNullable(aggregation);
        return this;
    }

    public QueryParametersBuilder search(Search search) {
        this.search = Optional.ofNullable(search);
        return this;
    }
}
