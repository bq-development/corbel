package io.corbel.event;

import java.util.Map;

import io.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Alberto J. Rubio
 */
public class NotificationEvent extends EventWithSpecificDomain {
    private String notificationId;
    private String recipient;
    private Map<String, String> properties;

    public NotificationEvent() {}

    public NotificationEvent(String notificationId, String recipient) {
        this(notificationId, recipient, null);
    }

    public NotificationEvent(String notificationId, String recipient, String domain) {
        super(domain);
        this.notificationId = notificationId;
        this.recipient = recipient;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NotificationEvent))
            return false;
        if (!super.equals(o))
            return false;

        NotificationEvent that = (NotificationEvent) o;

        if (notificationId != null ? !notificationId.equals(that.notificationId) : that.notificationId != null)
            return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null)
            return false;
        if (recipient != null ? !recipient.equals(that.recipient) : that.recipient != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (notificationId != null ? notificationId.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}
