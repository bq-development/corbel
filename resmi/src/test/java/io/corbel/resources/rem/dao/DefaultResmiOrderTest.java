package io.corbel.resources.rem.dao;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import io.corbel.resources.rem.model.ResourceUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.corbel.resources.rem.service.DefaultNamespaceNormalizer;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@RunWith(MockitoJUnitRunner.class)
public class DefaultResmiOrderTest {
    private static final String TEST_COLLECTION = "testCollection";
    private static final String TEST_ID = "testId";
    private static final String TEST_REL = "testRel";
    private static final String TEST_ID_RELATION_OBJECT = "relatedId";

    @Mock private MongoOperations mongoOperations;

    @Mock DefaultNamespaceNormalizer defaultNameNormalizer;

    @Mock ResmiOrder resmiOrderMock;

    private DefaultResmiOrder defaultResmiOrder;

    @Before
    public void setup() {
        when(defaultNameNormalizer.normalize(anyString())).then(returnsFirstArg());
        defaultResmiOrder = new DefaultResmiOrder(mongoOperations, defaultNameNormalizer);
    }


    @Test
    public void moveToFirstTest() {
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID, TEST_REL, TEST_ID_RELATION_OBJECT);
        RelationMoveOperation relationMoveOperation = new RelationMoveOperation(1);

        JsonObject elem1 = new JsonObject();
        elem1.addProperty("_order", 1.0d);
        JsonObject elem2 = new JsonObject();
        elem2.addProperty("_order", 2.0d);
        List<JsonObject> list = Arrays.asList(elem1, elem2);

        Mockito.when(mongoOperations.find(Mockito.any(Query.class), Mockito.eq(JsonObject.class), Mockito.anyString())).thenReturn(list);

        defaultResmiOrder.moveRelation(resourceUri, relationMoveOperation);

        Update update = new Update();
        update.set("_order", 0.0d);
        Mockito.verify(mongoOperations).updateFirst(Mockito.any(Query.class), Mockito.eq(update), Mockito.anyString());
    }

    @Test
    public void moveToMiddleTest() {
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID, TEST_REL, TEST_ID_RELATION_OBJECT);
        RelationMoveOperation relationMoveOperation = new RelationMoveOperation(2);

        JsonObject elem1 = new JsonObject();
        elem1.addProperty("_order", 1.0d);
        JsonObject elem2 = new JsonObject();
        elem2.addProperty("_order", 2.0d);
        List<JsonObject> list = Arrays.asList(elem1, elem2);

        Mockito.when(mongoOperations.find(Mockito.any(Query.class), Mockito.eq(JsonObject.class), Mockito.anyString())).thenReturn(list);

        defaultResmiOrder.moveRelation(resourceUri, relationMoveOperation);

        Update update = new Update();
        update.set("_order", 1.5d);
        Mockito.verify(mongoOperations).updateFirst(Mockito.any(Query.class), Mockito.eq(update), Mockito.anyString());
    }

    @Test
    public void moveToLastTest() {
        ResourceUri resourceUri = new ResourceUri(TEST_COLLECTION, TEST_ID, TEST_REL, TEST_ID_RELATION_OBJECT);
        RelationMoveOperation relationMoveOperation = new RelationMoveOperation(2);

        JsonObject elem1 = new JsonObject();
        elem1.addProperty("_order", 1.0d);
        List<JsonObject> list = Arrays.asList(elem1);

        Mockito.when(mongoOperations.find(Mockito.any(Query.class), Mockito.eq(JsonObject.class), Mockito.anyString())).thenReturn(list);

        JsonObject json = new JsonObject();
        json.add("counter", new JsonPrimitive(2.0d));
        Mockito.when(
                mongoOperations.findAndModify(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.any(),
                        Mockito.eq(JsonObject.class), Mockito.anyString())).thenReturn(json);

        defaultResmiOrder.moveRelation(resourceUri, relationMoveOperation);

        Update update = new Update();
        update.set("_order", 2.0d);
        Mockito.verify(mongoOperations).updateFirst(Mockito.any(Query.class), Mockito.eq(update), Mockito.anyString());
    }

}
