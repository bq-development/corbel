package io.corbel.notifications.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

/**
 * @author Francisco Sanchez
 */
public class NotificationTemplate {

	@Id
	private String id;

    @NotEmpty
	private String type;

    @NotEmpty
    private String sender;

    @NotEmpty
    @Template
	private String text;

    @Template
    private String title;

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

    public void updateTemplate(NotificationTemplate updateNotificationTemplate) {
        if(updateNotificationTemplate.getSender() != null) {
            setSender(updateNotificationTemplate.getSender());
        }
        if(updateNotificationTemplate.getText() != null) {
            setText(updateNotificationTemplate.getText());
        }
        if(updateNotificationTemplate.getTitle() != null) {
            setTitle(updateNotificationTemplate.getTitle());
        }
        if(updateNotificationTemplate.getType() != null) {
            setType(updateNotificationTemplate.getType());
        }
    }
}
