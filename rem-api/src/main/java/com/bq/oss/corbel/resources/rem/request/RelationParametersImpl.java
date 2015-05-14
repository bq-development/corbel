package com.bq.oss.corbel.resources.rem.request;

import java.util.Optional;

import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.parser.SortParser;

/**
 * @author Alexander De Leon
 *
 */
public class RelationParametersImpl extends CollectionParametersImpl implements RelationParameters {

    private final Optional<String> predicate;

    public RelationParametersImpl(int pageSize, int page, int maxPageSize, Optional<String> sort, Optional<String> query,
            QueryParser queryParser, Optional<String> aggregation, AggregationParser aggregationParser, SortParser sortParser,
            Optional<String> predicate, Optional<String> search) {
        super(pageSize, page, maxPageSize, sort, query, queryParser, aggregation, aggregationParser, sortParser, search);
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
