package io.corbel.resources.rem.utils;

import io.corbel.resources.rem.model.AclPermission;

/**
 * @author Cristian del Cerro
 */
public class AclUtils {

    public static String buildMessage(AclPermission permission) {
        return permission + " permission is required to perform the operation";
    }

}
