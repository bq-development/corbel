package com.bq.oss.corbel.resources.rem.dao;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.google.gson.JsonObject;

public interface ResmiOrder {
    void addNextOrderInRelation(String type, String id, String relation, JsonObject relationJson);

    void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation);

}
