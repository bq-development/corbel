package io.corbel.iam.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * @author Alberto J. Rubio
 */
public class TokenUpgradeGrant {

    private final Set<String> scopes;

    @JsonCreator
    public TokenUpgradeGrant(@JsonProperty("scopes") Set<String> scopes) {
        this.scopes = scopes;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenUpgradeGrant that = (TokenUpgradeGrant) o;

        return scopes != null ? scopes.equals(that.scopes) : that.scopes == null;

    }

    @Override
    public int hashCode() {
        return scopes != null ? scopes.hashCode() : 0;
    }
}
