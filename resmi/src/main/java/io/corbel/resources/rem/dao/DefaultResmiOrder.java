package io.corbel.resources.rem.dao;

import java.util.List;

import io.corbel.resources.rem.model.ResourceUri;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DefaultResmiOrder implements ResmiOrder {

    private static final String RELATION_CONCATENATOR = ".";
    private static final String RELATION_ORDER_COUNTER_KEY = "counter";
    private static final String ORDER_FIELD = "_order";
    private static final String RELATION_COUNTERS_COLLECTION = "relationCounters";
    private static final String _ID = "_id";
    private final MongoOperations mongoOperations;
    private final NamespaceNormalizer namespaceNormalizer;

    public DefaultResmiOrder(MongoOperations mongoOperations, NamespaceNormalizer namespaceNormalizer) {
        this.mongoOperations = mongoOperations;
        this.namespaceNormalizer = namespaceNormalizer;
    }

    @Override
    public void addNextOrderInRelation(String type, String id, String relation, JsonObject relationJson) {
        relationJson.add(ORDER_FIELD, new JsonPrimitive(nextOrderInRelation(type, id, relation)));
    }

    @Override
    public void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation) {
        String originCollection = namespaceNormalizer.normalize(uri.getType());
        String destCollection = namespaceNormalizer.normalize(uri.getRelation());

        if (relationMoveOperation.getValue() < 1) {
            throw new IllegalArgumentException("$pos must be greater or equal than 1");
        }
        String collection = originCollection + RELATION_CONCATENATOR + destCollection;;
        Query query = Query.query(Criteria.where(JsonRelation._SRC_ID).is(uri.getTypeId()).and(JsonRelation._DST_ID).ne(uri.getRelationId())).limit(2);
        if (relationMoveOperation.getValue() > 1) {
            query.skip((int) relationMoveOperation.getValue() - 2);
        }
        query.with(new org.springframework.data.domain.Sort(new Sort.Order(Sort.Direction.ASC, ORDER_FIELD)));

        List<JsonObject> list = mongoOperations.find(query, JsonObject.class, collection);

        double order;
        if (!list.isEmpty() && relationMoveOperation.getValue() == 1) {
            order = list.get(0).get(ORDER_FIELD).getAsDouble() - 1;
        } else if (list.size() == 2) {
            double order1 = list.get(0).get(ORDER_FIELD).getAsDouble();
            double order2 = list.get(1).get(ORDER_FIELD).getAsDouble();
            order = (order1 + order2) / 2;
            if (order == order1 || order == order2) {
                Update update = new Update();
                update.inc(ORDER_FIELD, 1);
                nextOrderInRelation(originCollection, uri.getTypeId(), destCollection);
                mongoOperations.updateMulti(Query.query(Criteria.where(JsonRelation._SRC_ID).is(uri.getTypeId()).and(ORDER_FIELD).gt(order1)), update,
                        collection);
                order = (order1 + order2 + 1) / 2;
            }
        } else {
            order = nextOrderInRelation(originCollection, uri.getTypeId(), destCollection);
        }

        Update update = new Update();
        update.set(ORDER_FIELD, order);
        mongoOperations.updateFirst(Query.query(Criteria.where(JsonRelation._SRC_ID).is(uri.getTypeId()).and(JsonRelation._DST_ID).is(uri.getRelationId())), update,
                collection);
    }

    private double nextOrderInRelation(String type, String id, String relation) {
        Query query = Query.query(Criteria.where(_ID).is(
                namespaceNormalizer.normalize(type).concat(id).concat(namespaceNormalizer.normalize(relation))));
        query.fields().include(RELATION_ORDER_COUNTER_KEY);
        Update update = new Update().inc(RELATION_ORDER_COUNTER_KEY, 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        JsonObject counter = mongoOperations.findAndModify(query, update, options, JsonObject.class, RELATION_COUNTERS_COLLECTION);
        return counter.get(RELATION_ORDER_COUNTER_KEY).getAsDouble();
    }


}
