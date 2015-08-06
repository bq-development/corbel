package io.corbel.iam.repository;

/**
 * @author Francisco Sanchez
 */
public class CreateUserException extends Exception {

    public CreateUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateUserException(String message) {
        super(message);
    }

}
