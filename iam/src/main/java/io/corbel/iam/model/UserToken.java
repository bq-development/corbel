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

    private Date expireAt;

    public UserToken() {}

    public UserToken(String token, String userId, Date expireAt) {
        this.token = token;
        this.userId = userId;
        this.expireAt = expireAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserToken))
            return false;

        UserToken userToken = (UserToken) o;

        if (expireAt != null ? !expireAt.equals(userToken.expireAt) : userToken.expireAt != null)
            return false;
        if (token != null ? !token.equals(userToken.token) : userToken.token != null)
            return false;
        if (userId != null ? !userId.equals(userToken.userId) : userToken.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (expireAt != null ? expireAt.hashCode() : 0);
        return result;
    }
}
