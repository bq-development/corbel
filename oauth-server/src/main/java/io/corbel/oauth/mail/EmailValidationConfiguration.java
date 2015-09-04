package io.corbel.oauth.mail;

/**
 * @author Alberto J. Rubio
 */
public class EmailValidationConfiguration extends NotificationConfiguration {

    private final boolean validationEnabled;

    public EmailValidationConfiguration(String notificationId, String clientUrl, long tokenDurationInSeconds, boolean validationEnabled) {
        super(notificationId, clientUrl, tokenDurationInSeconds);
        this.validationEnabled = validationEnabled;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }
}
