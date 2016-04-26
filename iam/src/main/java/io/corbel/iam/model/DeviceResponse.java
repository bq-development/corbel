package io.corbel.iam.model;

import java.util.Date;

/**
 * @author Francisco Sanchez
 */
public class DeviceResponse extends Entity {
    private String notificationUri;
    private String name;
    private String type;
    private Boolean notificationEnabled;
    private Date firstConnection;
    private Date lastConnection;

    public DeviceResponse() {}

    public DeviceResponse(Device device) {
        this.setId(device.getUid());
        notificationUri = device.getNotificationUri();
        name = device.getName();
        type = device.getType();
        notificationEnabled = device.isNotificationEnabled();
        firstConnection = device.getFirstConnection();
        lastConnection = device.getLastConnection();
    }

    public String getNotificationUri() {
        return notificationUri;
    }

    public DeviceResponse setNotificationUri(String notificationUri) {
        this.notificationUri = notificationUri;
        return this;
    }

    public String getName() {
        return name;
    }

    public DeviceResponse setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public DeviceResponse setType(String type) {
        this.type = type;
        return this;
    }

    public Boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public DeviceResponse setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
        return this;
    }

    public Date getFirstConnection() {
        return firstConnection;
    }

    public DeviceResponse setFirstConnection(Date firstConnection) {
        this.firstConnection = firstConnection;
        return this;
    }

    public Date getLastConnection() {
        return lastConnection;
    }

    public DeviceResponse setLastConnection(Date lastConnection) {
        this.lastConnection = lastConnection;
        return this;
    }
}
