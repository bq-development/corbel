package io.corbel.resources.rem.utils;

import io.corbel.resources.rem.model.AclPermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Cristian del Cerro
 */
public class AclUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AclUtils.class);

    private AclUtils() {}

    public static String buildMessage(AclPermission permission) {
        return permission + " permission is required to perform the operation";
    }

    public static boolean entityIsEmpty(MultivaluedMap<String, String> headers) {
        try {
            String contentLength = headers.getFirst("Content-Length");
            if (contentLength==null) {
                return true;
            }

            return Integer.parseInt(contentLength) == 0;
        } catch (Exception e) {
            LOG.error("Fail to check input stream availability", e);
            return true;
        }
    }

}
