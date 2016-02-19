package io.corbel.iam.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

/**
 * @author Francisco Sanchez
 */
public class Device extends Entity {

    private String domain;
    private String userId;
    @NotNull private String uid;
    private String notificationUri;
    private String name;
    private Type type;
    private Boolean notificationEnabled;
    private Date _createdAt;
    private Date _updatedAt;

    public String getDomain() {
        return domain;
    }

    public Device setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public Device setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Device setName(String name) {
        this.name = name;
        return this;
    }

    public String getNotificationUri() {
        return notificationUri;
    }

    public Device setNotificationUri(String notificationUri) {
        this.notificationUri = notificationUri;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Device setType(Type type) {
        this.type = type;
        return this;
    }

    public Boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public Device setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public Device setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public Date get_createdAt() {
        return _createdAt;
    }

    public Device set_createdAt(Date _createdAt) {
        this._createdAt = _createdAt;
        return this;
    }

    public Date get_updatedAt() {
        return _updatedAt;
    }

    public Device set_updatedAt(Date _updatedAt) {
        this._updatedAt = _updatedAt;
        return this;
    }

    public enum Type {
        Android, Apple
    }

}
