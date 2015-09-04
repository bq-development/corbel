package io.corbel.oauth.model;

/**
 * @author by Alberto J. Rubio
 */
public enum ResponseType {

    CODE, TOKEN, INVALID;

    public static ResponseType fromString(String name) {
        try {
            return name == null ? null : Enum.valueOf(ResponseType.class, name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INVALID;
        }
    }
}
