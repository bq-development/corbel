package io.corbel.resources.rem.dao;

import io.corbel.resources.rem.model.ResourceUri;
import com.google.gson.JsonObject;

public interface ResmiOrder {
    void addNextOrderInRelation(ResourceUri uri, JsonObject relationJson);

    void moveRelation(ResourceUri uri, RelationMoveOperation relationMoveOperation);

}
