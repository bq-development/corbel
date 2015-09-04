package io.corbel.oauth.mail;

/**
 * @author by Alberto J. Rubio
 */
public class NotificationConfiguration {

    private final String notificationId;
    private final String clientUrl;
    private final long tokenDurationInSeconds;

    public NotificationConfiguration(String notificationId, String clientUrl, long tokenDurationInSeconds) {
        this.notificationId = notificationId;
        this.clientUrl = clientUrl;
        this.tokenDurationInSeconds = tokenDurationInSeconds;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public long getTokenDurationInSeconds() {
        return tokenDurationInSeconds;
    }

}
