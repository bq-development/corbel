package io.corbel.resources.rem.model;

import java.util.List;

import javax.validation.constraints.NotNull;

public class ManagedCollection {

    @NotNull private String id;
    @NotNull private List<String> users;
    @NotNull private List<String> groups;

    public ManagedCollection() {}

    public ManagedCollection(String id, List<String> users, List<String> groups) {
        this.id = id;
        this.users = users;
        this.groups = groups;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ManagedCollection))
            return false;

        ManagedCollection that = (ManagedCollection) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (users != null ? !users.equals(that.users) : that.users != null)
            return false;
        return !(groups != null ? !groups.equals(that.groups) : that.groups != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (users != null ? users.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }

}
