package io.corbel.event;

import io.corbel.eventbus.EventWithSpecificDomain;

/**
 * Created by Francisco Sanchez on 14/10/15.
 */
public class DeviceEvent extends EventWithSpecificDomain {

    public enum Type {
        CREATED,
        UPDATED,
        DELETED
    }

    private Type type;
    private String deviceId;
    private String userId;
    private String deviceType;
    private String deviceName;


    public DeviceEvent() {}

    public DeviceEvent(Type type, String domain, String deviceId, String userId, String deviceType, String deviceName) {
        super(domain);
        this.type = type;
        this.deviceId = deviceId;
        this.userId = userId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
    }

    public DeviceEvent(Type type, String domainId, String deviceId, String userId) {
        this(type, domainId, deviceId, userId, null, null);
    }

    public Type getType() {
        return type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

}
