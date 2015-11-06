package io.corbel.evci.model;

/**
 * @author Alberto J. Rubio
 */
public class Header {

    private String domainId;
    private String clientId;
    private String userId;
    private String deviceId;

    public Header(String domainId, String clientId, String userId, String deviceId) {
        this.domainId = domainId;
        this.clientId = clientId;
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public Header() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (domainId != null ? !domainId.equals(header.domainId) : header.domainId != null) return false;
        if (clientId != null ? !clientId.equals(header.clientId) : header.clientId != null) return false;
        if (userId != null ? !userId.equals(header.userId) : header.userId != null) return false;
        return !(deviceId != null ? !deviceId.equals(header.deviceId) : header.deviceId != null);

    }

    @Override
    public int hashCode() {
        int result = domainId != null ? domainId.hashCode() : 0;
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        return result;
    }
}
