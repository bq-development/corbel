package io.corbel.resources.rem.dao;

import java.util.Arrays;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public class JsonRelation {

    private JsonRelation() {}

    public static final String ID = "id";
    public static final String _DST_ID = "_dst_id";
    public static final String _SRC_ID = "_src_id";
    private static final String[] ignoredFields = new String[] {ID, _DST_ID, _SRC_ID};

    public static JsonObject create(String srcId, String dstId) {
        JsonObject relation = new JsonObject();
        relation.addProperty(_SRC_ID, srcId);
        relation.addProperty(_DST_ID, dstId);

        return relation;
    }

    public static JsonObject create(String srcId, String dstId, JsonObject entity) {
        JsonObject relation = create(srcId, dstId);
        if (entity != null) {
            for (Entry<String, JsonElement> entry : entity.entrySet()) {
                if (!isIgnoreField(entry.getKey())) {
                    relation.add(entry.getKey(), entry.getValue());
                }
            }
        }
        return relation;
    }

    private static boolean isIgnoreField(String key) {
        return Arrays.asList(ignoredFields).contains(key);
    }

    public static boolean validateUri(String uri) {
        String[] uriParts = uri.split("/");
        return uriParts.length == 2;
    }

}
