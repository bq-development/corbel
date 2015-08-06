package io.corbel.resources.service;

import java.util.Set;

import io.corbel.resources.model.RelationSchema;
import io.corbel.resources.rem.plugin.RelationRegistry;
import io.corbel.resources.repository.RelationSchemaRepository;

/**
 * @author Rubén Carrasco
 * 
 */
public class DefaultRelationSchemaService implements RelationSchemaService {

    private final RelationSchemaRepository repository;
    private final RelationRegistry relationRegistry;

    public DefaultRelationSchemaService(RelationSchemaRepository repository, RelationRegistry relationRegistry) {
        this.repository = repository;
        this.relationRegistry = relationRegistry;
    }

    @Override
    public Set<String> getTypeRelations(String type) {
        Set<String> typeRelations = relationRegistry.getTypeRelations(type);
        RelationSchema relationSchema = repository.findOne(type);
        if (relationSchema != null) {
            Set<String> keySet = relationSchema.getRelations().keySet();
            typeRelations.addAll(keySet);
        }
        return typeRelations;
    }

    @Override
    public Set<String> getRelationFields(String type, String relation) {
        Set<String> relationFields = relationRegistry.getRelationFields(type, relation);
        RelationSchema relationSchema = repository.findOne(type);
        if (relationSchema != null) {
            Set<String> relations = relationSchema.getRelations().get(relation);
            if (relations != null) {
                relationFields.addAll(relations);
            }
        }
        return relationFields;
    }

}
