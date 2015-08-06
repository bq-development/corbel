package io.corbel.iam.model;

import java.util.Set;

import javax.validation.constraints.NotNull;

public class Group implements HasScopes {
    private String id;
    @NotNull private String name;
    private String domain;
    private Set<String> scopes;

    public Group() {}

    public Group(String id, String name, String domain, Set<String> scopes) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.scopes = scopes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean addScope(String scope) {
        return this.scopes.add(scope);
    }

    @Override
    public boolean removeScope(String scope) {
        return this.scopes.remove(scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Group))
            return false;

        Group group = (Group) o;

        if (id != null ? !id.equals(group.id) : group.id != null)
            return false;
        if (name != null ? !name.equals(group.name) : group.name != null)
            return false;
        if (domain != null ? !domain.equals(group.domain) : group.domain != null)
            return false;
        return !(scopes != null ? !scopes.equals(group.scopes) : group.scopes != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        return result;
    }
}
