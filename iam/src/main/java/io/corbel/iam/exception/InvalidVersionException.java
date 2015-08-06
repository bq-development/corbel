package io.corbel.iam.exception;

/**
 * @author Alberto J. Rubio
 */
public class InvalidVersionException extends UnauthorizedException {

    public InvalidVersionException(String message) {
        super(message);
    }
}
