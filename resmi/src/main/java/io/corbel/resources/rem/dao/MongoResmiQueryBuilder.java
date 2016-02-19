package io.corbel.resources.rem.dao;

import io.corbel.lib.queries.builder.QueryBuilder;
import io.corbel.lib.queries.mongo.builder.CriteriaBuilder;
import io.corbel.lib.queries.mongo.builder.MongoQueryBuilder;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.ResourceId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.List;

/**
 * @author Alberto J. Rubio
 *
 */
public class MongoResmiQueryBuilder extends MongoQueryBuilder {

    DateQueryNodeTransformer transformer = new DateQueryNodeTransformer();

    public MongoResmiQueryBuilder id(String id) {
        query.addCriteria(Criteria.where("_id").is(id));
        return this;
    }

    public MongoResmiQueryBuilder relationSubjectId(ResourceUri resourceUri) {
        if (resourceUri.isRelation() && !resourceUri.isTypeWildcard()) {
            relationSubjectId(new ResourceId(resourceUri.getTypeId()));
        }
        return this;
    }

    public MongoResmiQueryBuilder relationSubjectId(ResourceId id) {
        if (!id.isWildcard()) {
            query.addCriteria(Criteria.where(JsonRelation._SRC_ID).is(id.getId()));
        }
        return this;
    }

    public MongoResmiQueryBuilder relationDestinationId(String id) {
        query.addCriteria(Criteria.where(JsonRelation._DST_ID).is(id));
        return this;
    }

    public MongoResmiQueryBuilder textSearch(String textSearchQuery)
    {
        if (textSearchQuery != null)
        {
            final TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(textSearchQuery);
            query.addCriteria(textCriteria);
        }
        return this;
    }

    @Override
    public QueryBuilder query(ResourceQuery resourceQuery) {
        if (resourceQuery != null) {
            query.addCriteria(CriteriaBuilder.buildFromResourceQuery(resourceQuery, transformer));
        }
        return this;
    }

    @Override
    public QueryBuilder query(List<ResourceQuery> resourceQueries) {
        if (resourceQueries != null && !resourceQueries.isEmpty()) {
            query.addCriteria(CriteriaBuilder.buildFromResourceQueries(resourceQueries, transformer));
        }
        return this;
    }

}
