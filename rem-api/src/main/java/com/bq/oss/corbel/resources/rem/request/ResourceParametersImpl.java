package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.parser.AggregationParser;
import com.bq.oss.lib.queries.parser.QueryParser;
import com.bq.oss.lib.queries.parser.SortParser;
import com.bq.oss.lib.queries.request.*;

/**
 * Created by Francisco Sanchez on 26/05/15.
 */
public class ResourceParametersImpl extends QueryParameters implements ResourceParameters {

    public ResourceParametersImpl(Pagination pagination, Optional<Sort> sort, Optional<List<ResourceQuery>> queries,
            Optional<List<ResourceQuery>> conditions, Optional<Aggregation> aggreagation, Optional<ResourceSearch> search) {
        super(pagination, sort, queries, conditions, aggreagation, search);
    }

    public ResourceParametersImpl(QueryParameters queryParameters) {
        super(queryParameters);
    }

}
