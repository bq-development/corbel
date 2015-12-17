package io.corbel.resources.rem.service;

import java.util.Set;

import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.model.SearchResource;

/**
 * @author Francisco Sanchez
 */
public interface SearchableFieldsRegistry {
    Set<String> getFieldsFromType(String domain, String type);

    Set<String> getFieldsFromRelation(String domain, String type, String relation);

    void addFields(SearchResource searchResource);

    Set<String> getFieldsFromResourceUri(ResourceUri resourceUri);
}
