package io.corbel.notifications.model;

import org.springframework.data.annotation.Id;

/**
 * @author Francisco Sanchez
 */
public class NotificationTemplate {

    @Id
    private String id;

    private String name;

    private String domain;

    private String type;

    private String sender;

    @Template
    private String text;

    @Template
    private String title;

    public NotificationTemplate() {}

    public  NotificationTemplate(String domain, NotificationTemplateApi notificationTemplateApi) {
        this.name = notificationTemplateApi.getId();
        this.domain = domain;
        this.type = notificationTemplateApi.getType();
        this.sender = notificationTemplateApi.getSender();
        this.text = notificationTemplateApi.getText();
        this.title = notificationTemplateApi.getTitle();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public void updateTemplate(NotificationTemplate updateNotificationTemplate) {
        if (updateNotificationTemplate.getSender() != null) {
            setSender(updateNotificationTemplate.getSender());
        }
        if (updateNotificationTemplate.getDomain() != null) {
            setDomain(updateNotificationTemplate.getDomain());
        }
        if (updateNotificationTemplate.getName() != null) {
            setName(updateNotificationTemplate.getName());
        }
        if (updateNotificationTemplate.getText() != null) {
            setText(updateNotificationTemplate.getText());
        }
        if (updateNotificationTemplate.getTitle() != null) {
            setTitle(updateNotificationTemplate.getTitle());
        }
        if (updateNotificationTemplate.getType() != null) {
            setType(updateNotificationTemplate.getType());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationTemplate that = (NotificationTemplate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return !(title != null ? !title.equals(that.title) : that.title != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
