package com.bq.oss.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import com.bq.oss.lib.queries.request.*;

/**
 * @author Alexander De Leon
 * 
 */
public interface CollectionParameters {

    Pagination getPagination();

    @Deprecated
    Optional<ResourceQuery> getQuery();

    Optional<List<ResourceQuery>> getQueries();

    Optional<ResourceSearch> getSearch();

    Optional<Sort> getSort();

    Optional<Aggregation> getAggregation();

    void setPagination(Pagination pagination);

    @Deprecated
    void setQuery(Optional<ResourceQuery> resource);

    void setQueries(Optional<List<ResourceQuery>> resources);

    void setSearch(Optional<ResourceSearch> search);

    void setSort(Optional<Sort> sort);

    void setAggregation(Optional<Aggregation> aggregation);

}
