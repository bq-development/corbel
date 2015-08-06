package io.corbel.iam.exception;

/**
 * @author Alexander De Leon
 * 
 */
public class UnauthorizedException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }

}
