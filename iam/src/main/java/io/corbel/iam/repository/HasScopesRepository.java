package io.corbel.iam.repository;

/**
 * @author Alexander De Leon
 * 
 */
public interface HasScopesRepository<ID> {

    String FIELD_ID = "_id";
    String FIELD_SCOPES = "scopes";


    void addScopes(ID id, String... scopes);

    void removeScopes(ID id, String... scopes);

    void removeScopes(String... scopes);
}
