package io.corbel.resources.rem.dao;

import io.corbel.resources.rem.model.ResourceUri;
import com.google.gson.JsonObject;

public interface ResmiOrder {
    void addNextOrderInRelation(String type, String id, String relation, JsonObject relationJson);

    void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation);

}
