package io.corbel.iam.model;

import java.util.Date;
import java.util.Optional;

/**
 * @author Francisco Sanchez
 */
public class Device extends Entity {

    public static final String USER_ID_FIELD = "userId";
    public static final String LAST_CONNECTION_FIELD = "lastConnection";

    private String domain;
    private String userId;
    private String uid;
    private String notificationUri;
    private String name;
    private String type;
    private Boolean notificationEnabled;
    private Date firstConnection;
    private Date lastConnection;

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

    public String getType() {
        return type;
    }

    public Device setType(String type) {
        this.type = Optional.ofNullable(type).map(String::toUpperCase).orElse(type);
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

    public Date getFirstConnection() {
        return firstConnection;
    }

    public Device setFirstConnection(Date firstConnection) {
        this.firstConnection = firstConnection;
        return this;
    }

    public Date getLastConnection() {
        return lastConnection;
    }

    public Device setLastConnection(Date lastConnection) {
        this.lastConnection = lastConnection;
        return this;
    }
}
