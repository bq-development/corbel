package io.corbel.resources.rem.model;

import java.util.List;

import org.springframework.util.StringUtils;

public class ManagedCollection {

    private List<String> users;
    private List<String> groups;
    private String domain;
    private String collectionName;
    private AclPermission defaultPermission;

    public ManagedCollection() {}

    public ManagedCollection(String collectionName, List<String> users, List<String> groups) {
        this.collectionName = collectionName;
        this.users = users;
        this.groups = groups;
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

    public AclPermission getDefaultPermission() {
        return defaultPermission;
    }

    public void setDefaultPermission(AclPermission defaultPermission) {
        this.defaultPermission = defaultPermission;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public boolean validate() {
        return users != null && groups != null && !StringUtils.isEmpty(collectionName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collectionName == null) ? 0 : collectionName.hashCode());
        result = prime * result + ((defaultPermission == null) ? 0 : defaultPermission.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((users == null) ? 0 : users.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ManagedCollection other = (ManagedCollection) obj;
        if (collectionName == null) {
            if (other.collectionName != null) {
                return false;
            }
        } else if (!collectionName.equals(other.collectionName)) {
            return false;
        }
        if (defaultPermission != other.defaultPermission) {
            return false;
        }
        if (domain == null) {
            if (other.domain != null) {
                return false;
            }
        } else if (!domain.equals(other.domain)) {
            return false;
        }
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (users == null) {
            if (other.users != null) {
                return false;
            }
        } else if (!users.equals(other.users)) {
            return false;
        }
        return true;
    }



}
