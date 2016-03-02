package io.corbel.iam.model;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Set;

/**
 * @author Cristian del Cerro
 */
public class UserToken {

    public static final String EXPIRABLE_FIELD = "expireAt";

    @Id private String token;
    private String userId;
    private String deviceId;
    private Date expireAt;
    private Set<Scope> scopes;

    public UserToken() {}

    public UserToken(String token, String userId, String deviceId, Date expireAt, Set<Scope> scopes) {
        this.token = token;
        this.userId = userId;
        this.deviceId = deviceId;
        this.expireAt = expireAt;
        this.scopes = scopes;
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

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserToken userToken = (UserToken) o;

        if (token != null ? !token.equals(userToken.token) : userToken.token != null) return false;
        if (userId != null ? !userId.equals(userToken.userId) : userToken.userId != null) return false;
        if (deviceId != null ? !deviceId.equals(userToken.deviceId) : userToken.deviceId != null) return false;
        if (expireAt != null ? !expireAt.equals(userToken.expireAt) : userToken.expireAt != null) return false;
        if (scopes != null ? !scopes.equals(userToken.scopes) : userToken.scopes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (expireAt != null ? expireAt.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        return result;
    }
}
