package io.corbel.resources.rem.acl;

/**
 * @author Cristian del Cerro
 */
public enum AclPermission {
    READ, WRITE, ADMIN;

    public boolean canPerform(AclPermission operation) {
        return this.ordinal() >= operation.ordinal();
    }
}
