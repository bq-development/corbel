package com.bq.oss.corbel.iam.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alberto J. Rubio
 *
 */
public class Group {

    private String name;
    private Set<String> scopes = new HashSet<>();

    public Group(String name, Set<String> scopes) {
        this.name = name;
        this.scopes = scopes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
