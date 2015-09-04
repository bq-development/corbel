package io.corbel.oauth.repository;

/**
 * @author Francisco Sanchez
 */
public class CreateUserException extends Exception {
    private static final long serialVersionUID = 1L;

    public CreateUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateUserException(String message) {
        super(message);
    }

    public static class DuplicatedUser extends CreateUserException {

        private static final long serialVersionUID = 1L;

        public DuplicatedUser() {
            super("Duplicated user");
        }

    }

}
