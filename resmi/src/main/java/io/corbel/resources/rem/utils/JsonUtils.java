package io.corbel.resources.rem.utils;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Alberto J. Rubio
 *
 */
public final class JsonUtils {

    private JsonUtils () {}

    public static JsonArray convertToArray(List<JsonObject> jsonObjects) {
        JsonArray jsonArray = new JsonArray();
        for (JsonObject jsonObject : jsonObjects) {
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
