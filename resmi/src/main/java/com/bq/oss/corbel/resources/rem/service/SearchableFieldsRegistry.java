package com.bq.oss.corbel.resources.rem.service;

import java.util.Set;

import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchableFields;

/**
 * @author Francisco Sanchez
 */
public interface SearchableFieldsRegistry {
    public Set<String> getFieldsFromType(String type);

    public Set<String> getFieldsFromRelation(String type, String relation);

    public void addFields(SearchableFields searchableFields);

    Set<String> getFieldsFromResourceUri(ResourceUri resourceUri);
}
