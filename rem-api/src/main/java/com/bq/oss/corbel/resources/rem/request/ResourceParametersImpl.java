package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.request.Aggregation;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Search;
import com.bq.oss.lib.queries.request.Sort;

/**
 * Created by Francisco Sanchez on 26/05/15.
 */
public class ResourceParametersImpl extends QueryParameters implements ResourceParameters {

    public ResourceParametersImpl(Pagination pagination, Optional<Sort> sort, Optional<List<ResourceQuery>> queries,
            Optional<List<ResourceQuery>> conditions, Optional<Aggregation> aggreagation, Optional<Search> search) {
        super(pagination, sort, queries, conditions, aggreagation, search);
    }

    public ResourceParametersImpl(QueryParameters queryParameters) {
        super(queryParameters);
    }

}
