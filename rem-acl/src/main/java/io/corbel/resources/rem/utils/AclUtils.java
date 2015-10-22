package io.corbel.resources.rem.utils;

import io.corbel.resources.rem.acl.AclPermission;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Cristian del Cerro
 */
public class AclUtils {

    public static String buildMessage(AclPermission permission) {
        return permission + " permission is required to perform the operation";
    }

    public static boolean entityIsEmpty(InputStream entity) {
        try {
            return entity.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }
}
