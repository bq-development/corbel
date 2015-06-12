package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.parser.SortParser;
import com.bq.oss.lib.queries.request.*;

/**
 * @author Alexander De Leon
 *
 */
public class RelationParametersImpl extends CollectionParametersImpl implements RelationParameters {

    private final Optional<String> predicate;

    public RelationParametersImpl(Pagination pagination, Optional<Sort> sort, Optional<List<ResourceQuery>> queries,
            Optional<List<ResourceQuery>> conditions, Optional<Aggregation> aggreagation, Optional<ResourceSearch> search,
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
