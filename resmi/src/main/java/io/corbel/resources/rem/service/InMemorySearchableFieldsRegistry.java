package io.corbel.resources.rem.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.model.SearchResource;

/**
 * @author Francisco Sanchez
 */
public class InMemorySearchableFieldsRegistry implements SearchableFieldsRegistry {
    private final HashMap<ResourceUri, Set<String>> searchableFields;

    public InMemorySearchableFieldsRegistry() {
        searchableFields = new HashMap<>();
    }

    @Override
    public Set<String> getFieldsFromType(String domain, String type) {
        return getFieldsFromResourceUri(new ResourceUri(domain, type));
    }

    @Override
    public Set<String> getFieldsFromRelation(String domain, String type, String relation) {
        return getFieldsFromResourceUri(new ResourceUri(domain, type, null, relation));
    }

    @Override
    public Set<String> getFieldsFromResourceUri(ResourceUri resourceUri) {
        ResourceUri searchableResource = new ResourceUri(resourceUri.getDomain(), resourceUri.getType(), null, resourceUri.getRelation());
        return searchableFields.getOrDefault(searchableResource, Collections.<String>emptySet());
    }

    @Override
    public void addFields(SearchResource searchResource) {
        ResourceUri resourceUri = searchResource.getResourceUri();
        ResourceUri searchableResource = new ResourceUri(resourceUri.getDomain(), resourceUri.getType(), null, resourceUri.getRelation());
        this.searchableFields.put(searchableResource, searchResource.getFields());
    }

}
