package io.corbel.notifications.model;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Cristian del Cerro
 */
public class Notification {

    @NotNull
    private String notificationId;
    @NotNull
    private String recipient;

    private Map<String, String> properties;

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
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;

        Notification that = (Notification) o;

        if (notificationId != null ? !notificationId.equals(that.notificationId) : that.notificationId != null)
            return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        if (recipient != null ? !recipient.equals(that.recipient) : that.recipient != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = notificationId != null ? notificationId.hashCode() : 0;
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}
