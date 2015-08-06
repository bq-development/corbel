package io.corbel.resources.service;

import java.util.Set;

import org.springframework.cache.annotation.Cacheable;

/**
 * @author Rubén Carrasco
 * 
 */
public interface RelationSchemaService {

    String RELATION_FIELDS_CACHE = "relationFieldsCache";
    String TYPE_RELATIONS_CACHE = "typeRelationsCache";

    @Cacheable(TYPE_RELATIONS_CACHE)
    Set<String> getTypeRelations(String type);

    @Cacheable(RELATION_FIELDS_CACHE)
    Set<String> getRelationFields(String type, String relation);
}
