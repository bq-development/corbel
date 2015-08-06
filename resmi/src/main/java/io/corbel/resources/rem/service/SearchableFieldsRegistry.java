package io.corbel.resources.rem.service;

import java.util.Set;

import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.model.SearchResource;

/**
 * @author Francisco Sanchez
 */
public interface SearchableFieldsRegistry {
    public Set<String> getFieldsFromType(String type);

    public Set<String> getFieldsFromRelation(String type, String relation);

    public void addFields(SearchResource searchResource);

    Set<String> getFieldsFromResourceUri(ResourceUri resourceUri);
}
