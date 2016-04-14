package io.corbel.iam.model;

import java.util.Objects;
import java.util.Set;

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
    private final Set<String> scopes;

    @JsonCreator
    public TokenGrant(@JsonProperty("accessToken") String accessToken, @JsonProperty("expiresAt") long expiresAt,
            @JsonProperty("refreshToken") String refreshToken, @JsonProperty("scopes") Set<String> scopes) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.scopes = scopes;
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

    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, expiresAt, scopes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TokenGrant)) {
            return false;
        }
        TokenGrant that = (TokenGrant) obj;
        return Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.refreshToken, that.refreshToken)
                && Objects.equals(this.expiresAt, that.expiresAt) && Objects.equals(this.scopes, that.scopes);
    }
}
