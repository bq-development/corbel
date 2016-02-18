package io.corbel.iam.model;

import java.util.Date;

import org.springframework.data.annotation.Id;

/**
 * @author Cristian del Cerro
 */
public class UserToken {

    public static final String EXPIRABLE_FIELD = "expireAt";

    @Id private String token;
    private String userId;
    private String deviceId;
    private Date expireAt;

    public UserToken() {}

    public UserToken(String token, String userId, Date expireAt) {
        this(token, userId, null, expireAt);
    }

    public UserToken(String token, String userId, String deviceId, Date expireAt) {
        this.token = token;
        this.userId = userId;
        this.deviceId = deviceId;
        this.expireAt = expireAt;
    }

    public String getToken() {
        return token;
    }

    public UserToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserToken setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public UserToken setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public UserToken setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserToken userToken = (UserToken) o;

        if (token != null ? !token.equals(userToken.token) : userToken.token != null) return false;
        if (userId != null ? !userId.equals(userToken.userId) : userToken.userId != null) return false;
        if (deviceId != null ? !deviceId.equals(userToken.deviceId) : userToken.deviceId != null) return false;
        return expireAt != null ? expireAt.equals(userToken.expireAt) : userToken.expireAt == null;

    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (expireAt != null ? expireAt.hashCode() : 0);
        return result;
    }
}
