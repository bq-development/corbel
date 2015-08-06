package io.corbel.iam.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alexander De Leon
 * 
 */
public class TokenGrant {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresAt;

    @JsonCreator
    public TokenGrant(@JsonProperty("accessToken") String accessToken, @JsonProperty("expiresAt") long expiresAt,
            @JsonProperty("refreshToken") String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, expiresAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TokenGrant)) {
            return false;
        }
        TokenGrant that = (TokenGrant) obj;
        return Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.refreshToken, that.refreshToken)
                && Objects.equals(this.expiresAt, that.expiresAt);
    }
}
