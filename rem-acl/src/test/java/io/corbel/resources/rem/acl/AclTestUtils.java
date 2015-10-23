package io.corbel.resources.rem.acl;

import io.corbel.resources.rem.service.DefaultAclResourcesService;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Cristian del Cerro
 */
public class AclTestUtils {

    public static JsonObject getEntity(String userId, String permission) {
        JsonObject aclValue = new JsonObject();
        aclValue.addProperty(DefaultAclResourcesService.PERMISSION, permission);
        aclValue.add(DefaultAclResourcesService.PROPERTIES, new JsonObject());
        JsonObject acl = new JsonObject();
        acl.add(userId, aclValue);
        JsonObject object = new JsonObject();
        object.add("_acl", acl);
        return object;
    }

    public static JsonObject getEntityWithoutAcl() {
        JsonObject object = new JsonObject();
        object.add("field1", new JsonPrimitive("fieldContent"));
        return object;
    }
}
