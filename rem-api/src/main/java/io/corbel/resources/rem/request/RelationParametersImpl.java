package io.corbel.resources.rem.request;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.Aggregation;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.lib.queries.request.Sort;

import java.util.List;
import java.util.Optional;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RelationParametersImpl other = (RelationParametersImpl) obj;
        if (predicate == null) {
            if (other.predicate != null) {
                return false;
            }
        } else if (!predicate.equals(other.predicate)) {
            return false;
        }
        return true;
    }
}
