package com.bq.oss.corbel.resources.rem.dao;

import com.google.gson.JsonObject;

public interface ResmiOrder {
    void addNextOrderInRelation(String type, String id, String relation, JsonObject relationJson);

    void moveElement(String type, String id, String relation, String uri, RelationMoveOperation relationMoveOperation);

}
