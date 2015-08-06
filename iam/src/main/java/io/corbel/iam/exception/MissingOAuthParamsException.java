package io.corbel.iam.exception;

public class MissingOAuthParamsException extends Exception {

    private static final long serialVersionUID = 1L;

    public MissingOAuthParamsException(String message) {
        super(message);
    }

}
