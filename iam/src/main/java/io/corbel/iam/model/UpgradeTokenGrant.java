package io.corbel.iam.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Alberto J. Rubio
 */
public class UpgradeTokenGrant {

    private final List<String> scopes;

    @JsonCreator
    public UpgradeTokenGrant(@JsonProperty("scopes") List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopes() {
        return scopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpgradeTokenGrant that = (UpgradeTokenGrant) o;

        return scopes != null ? scopes.equals(that.scopes) : that.scopes == null;

    }

    @Override
    public int hashCode() {
        return scopes != null ? scopes.hashCode() : 0;
    }
}
