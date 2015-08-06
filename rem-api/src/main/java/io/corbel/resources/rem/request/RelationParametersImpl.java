package io.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.lib.queries.request.Sort;

/**
 * @author Alexander De Leon
 *
 */
public class RelationParametersImpl extends CollectionParametersImpl implements RelationParameters {

    private final Optional<String> predicate;

    public RelationParametersImpl(Pagination pagination, Optional<Sort> sort, Optional<List<ResourceQuery>> queries,
            Optional<List<ResourceQuery>> conditions, Optional<Aggregation> aggreagation, Optional<Search> search,
            Optional<String> predicate) {
        super(pagination, sort, queries, conditions, aggreagation, search);
        this.predicate = predicate;
    }

    public RelationParametersImpl(QueryParameters queryParameters, Optional<String> predicate) {
        super(queryParameters);
        this.predicate = predicate;
    }

    @Override
    public Optional<String> getPredicateResource() {
        return predicate;
    }

}
