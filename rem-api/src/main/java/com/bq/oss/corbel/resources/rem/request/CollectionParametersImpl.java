package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.request.*;

/**
 * @author Alexander De Leon
 *
 */
public class CollectionParametersImpl extends QueryParameters implements CollectionParameters {

    public CollectionParametersImpl(Pagination pagination, Optional<Sort> sort, Optional<List<ResourceQuery>> queries,
            Optional<List<ResourceQuery>> conditions, Optional<Aggregation> aggregation, Optional<ResourceSearch> search) {
        super(pagination, sort, queries, conditions, aggregation, search);
    }

    public CollectionParametersImpl(QueryParameters queryParameters) {
        super(queryParameters);
    }

}
