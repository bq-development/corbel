package io.corbel.notifications.model;


import org.hibernate.validator.constraints.NotEmpty;

public class NotificationTemplateApi {

    @NotEmpty
    private String id;

    @NotEmpty
    private String type;

    @NotEmpty
    private String sender;

    @NotEmpty
    private String text;

    private String title;

    public NotificationTemplateApi() {
    }

    public NotificationTemplateApi(NotificationTemplate notificationTemplate) {
        this.id = notificationTemplate.getName();
        this.type = notificationTemplate.getType();
        this.sender = notificationTemplate.getSender();
        this.text = notificationTemplate.getText();
        this.title = notificationTemplate.getTitle();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationTemplateApi that = (NotificationTemplateApi) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return !(title != null ? !title.equals(that.title) : that.title != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
