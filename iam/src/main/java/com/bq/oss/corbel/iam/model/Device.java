package com.bq.oss.corbel.iam.model;

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
    private boolean notificationEnabled;

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

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public Device setNotificationEnabled(boolean notificationEnabled) {
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

    public enum Type {
        Android, Apple
    }

}
