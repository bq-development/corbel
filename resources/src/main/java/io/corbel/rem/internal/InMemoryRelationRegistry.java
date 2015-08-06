package io.corbel.rem.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.corbel.resources.rem.plugin.RelationRegistry;

/**
 * @author Rubén Carrasco
 * 
 */
@Component public class InMemoryRelationRegistry implements RelationRegistry {

    private final Map<String, Map<String, Set<String>>> relationFields = new ConcurrentHashMap<String, Map<String, Set<String>>>();

    @Override
    public void addTypeRelation(String type, Set<String> relations) {
        for (String relation : relations) {
            addRelationFields(type, relation, new HashSet<String>());
        }
    }

    @Override
    public Set<String> getTypeRelations(String type) {
        Map<String, Set<String>> map = relationFields.get(type);
        return map != null ? new HashSet<String>(map.keySet()) : new HashSet<String>();
    }

    @Override
    public void addRelationFields(String type, String relation, Set<String> fields) {
        Map<String, Set<String>> typeRelations = relationFields.get(type);
        if (typeRelations == null) {
            typeRelations = new HashMap<String, Set<String>>();
            relationFields.put(type, typeRelations);
        }
        Set<String> fieldsSet = typeRelations.get(relation);
        if (fieldsSet == null) {
            fieldsSet = new HashSet<String>();
            typeRelations.put(relation, fieldsSet);
        }
        fieldsSet.addAll(fields);
    }

    @Override
    public Set<String> getRelationFields(String type, String relation) {
        Map<String, Set<String>> map = relationFields.get(type);
        return map != null ? map.get(relation) : new HashSet<String>();
    }

}
