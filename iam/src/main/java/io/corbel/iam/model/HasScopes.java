package io.corbel.iam.model;

import java.util.Set;

/**
 * @author Alexander De Leon
 * 
 */
public interface HasScopes {

    Set<String> getScopes();

    void setScopes(Set<String> scopes);

    boolean addScope(String scope);

    boolean removeScope(String scope);

}
