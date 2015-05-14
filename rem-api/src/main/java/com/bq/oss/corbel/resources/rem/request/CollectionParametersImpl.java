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
public class CollectionParametersImpl extends QueryParameters implements CollectionParameters {

    public CollectionParametersImpl(int pageSize, int page, int maxPageSize, Optional<String> sort, Optional<String> query,
            QueryParser queryParser, Optional<String> aggregation, AggregationParser aggregationParser, SortParser sortParser,
            Optional<String> search) {
        super(pageSize, page, maxPageSize, sort, query, queryParser, aggregation, aggregationParser, sortParser, search);
    }

    public CollectionParametersImpl(QueryParameters queryParameters) {
        super(queryParameters);
    }

}
