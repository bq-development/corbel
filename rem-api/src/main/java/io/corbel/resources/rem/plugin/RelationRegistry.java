package io.corbel.resources.rem.plugin;

import java.util.Set;

public interface RelationRegistry {

    void addTypeRelation(String type, Set<String> relations);

    Set<String> getTypeRelations(String type);

    void addRelationFields(String type, String relation, Set<String> fields);

    Set<String> getRelationFields(String type, String relation);
}
