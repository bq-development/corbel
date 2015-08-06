package io.corbel.resources.rem.dao;

import org.junit.Assert;
import org.junit.Test;

public class RelationMoveOperationTest {

    @Test
    public void posTest() {
        RelationMoveOperation moveOperation = RelationMoveOperation.create("$pos(2)");

        Assert.assertEquals(2, moveOperation.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongOperationTest() {
        RelationMoveOperation moveOperation = RelationMoveOperation.create("$wrong(2)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongFormatTest() {
        RelationMoveOperation moveOperation = RelationMoveOperation.create("pos(2)");
    }
}
