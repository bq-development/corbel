package io.corbel.iam.exception;

/**
 * @author Rubén Carrasco
 *
 */
public class UserProfileConfigurationException extends Exception {

    private static final long serialVersionUID = -6336607321264103058L;

    public UserProfileConfigurationException(String message, Exception e) {
        super(message, e);
    }

}
