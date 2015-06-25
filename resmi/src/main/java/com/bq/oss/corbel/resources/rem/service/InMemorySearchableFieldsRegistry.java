package com.bq.oss.corbel.resources.rem.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchResource;

/**
 * @author Francisco Sanchez
 */
public class InMemorySearchableFieldsRegistry implements SearchableFieldsRegistry {
    private final HashMap<ResourceUri, Set<String>> searchableFields;

    public InMemorySearchableFieldsRegistry() {
        searchableFields = new HashMap<>();
    }

    @Override
    public Set<String> getFieldsFromType(String type) {
        return searchableFields.getOrDefault(new ResourceUri(type), Collections.<String>emptySet());
    }

    @Override
    public Set<String> getFieldsFromRelation(String type, String relation) {
        return searchableFields.getOrDefault(new ResourceUri(type, null, relation), Collections.<String>emptySet());
    }

    @Override
    public Set<String> getFieldsFromResourceUri(ResourceUri resourceUri) {
        return getFieldsFromRelation(resourceUri.getType(), resourceUri.getRelation());
    }

    @Override
    public void addFields(SearchResource searchResource) {
        this.searchableFields.put(searchResource.getResourceUri(), searchResource.getFields());
    }

}
