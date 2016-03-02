package io.corbel.resources.rem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import io.corbel.resources.rem.model.AclPermission;

/**
 * @author Cristian del Cerro
 */
public class AclUtils {

    private AclUtils() {}

    public static String buildMessage(AclPermission permission) {
        return permission + " permission is required to perform the operation";
    }

    public static boolean entityIsEmpty(Optional<InputStream> entity) {
        return !entity.filter(e -> !entityIsEmpty(e)).isPresent();
    }

    public static boolean entityIsEmpty(InputStream entity) {
        try {
            return entity.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }
}
