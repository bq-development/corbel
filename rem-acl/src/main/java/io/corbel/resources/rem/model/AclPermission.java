package io.corbel.resources.rem.model;

/**
 * @author Cristian del Cerro
 */
public enum AclPermission {
    NONE, READ, WRITE, ADMIN;

    public boolean canPerform(AclPermission operation) {
        return this.ordinal() >= operation.ordinal();
    }
}
