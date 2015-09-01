package io.corbel.iam.exception;

/**
 * Created by Francisco Sanchez on 1/09/15.
 */
public class NotExistentScopeException extends Exception {
    public NotExistentScopeException(String message) {
        super(message);
    }
}
