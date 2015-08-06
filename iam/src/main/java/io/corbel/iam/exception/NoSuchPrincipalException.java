package io.corbel.iam.exception;

/**
 * @author Alberto J. Rubio
 */
public class NoSuchPrincipalException extends UnauthorizedException {

    public NoSuchPrincipalException(String message) {
        super(message);
    }
}
