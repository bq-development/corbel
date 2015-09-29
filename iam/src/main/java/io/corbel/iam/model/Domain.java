package io.corbel.iam.model;

import java.util.*;

/**
 * A domain is a logical grouping of {@link User}s and {@link Client}s
 * 
 * @author Alexander De Leon
 * 
 */
public class Domain extends TraceableEntity implements HasScopes {

    public static final String ID_SEPARATOR = ":";

    private String description;
    private String allowedDomains;
    private Set<String> scopes = new HashSet<>();
    private Set<String> defaultScopes = new HashSet<>();
    private Map<String, Map<String, String>> authConfigurations = new HashMap<>();
    private Set<String> userProfileFields = new HashSet<>();
    private Map<String, Boolean> capabilities = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Map<String, String>> getAuthConfigurations() {
        return authConfigurations;
    }

    public Set<String> getDefaultScopes() {
        return defaultScopes;
    }

    public void setDefaultScopes(Set<String> defaultScopes) {
        this.defaultScopes = defaultScopes;
    }

    public void setAuthConfigurations(Map<String, Map<String, String>> authConfigurations) {
        this.authConfigurations = authConfigurations;
    }

    public String getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(String allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public Set<String> getUserProfileFields() {
        return userProfileFields;
    }

    public void setUserProfileFields(Set<String> userProfileFields) {
        this.userProfileFields = userProfileFields;
    }

    public Map<String, Boolean> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Boolean> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean addScope(String scope) {
        return scopes.add(scope);
    }

    @Override
    public boolean removeScope(String scope) {
        return scopes.remove(scope);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Domain domain = (Domain) o;

        if (description != null ? !description.equals(domain.description) : domain.description != null) return false;
        if (allowedDomains != null ? !allowedDomains.equals(domain.allowedDomains) : domain.allowedDomains != null)
            return false;
        if (scopes != null ? !scopes.equals(domain.scopes) : domain.scopes != null) return false;
        if (defaultScopes != null ? !defaultScopes.equals(domain.defaultScopes) : domain.defaultScopes != null)
            return false;
        if (authConfigurations != null ? !authConfigurations.equals(domain.authConfigurations) : domain.authConfigurations != null)
            return false;
        if (userProfileFields != null ? !userProfileFields.equals(domain.userProfileFields) : domain.userProfileFields != null)
            return false;
        return !(capabilities != null ? !capabilities.equals(domain.capabilities) : domain.capabilities != null);

    }
}
