package io.corbel.resources.rem.model;

import java.util.Set;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Francisco Sanchez
 */
public class SearchResource {

    public static final String DEFAULT_FIELD_TYPE = "string";

    @Id private ResourceUri resourceUri;
    private Set<String> fields;

    public SearchResource() {}

    public SearchResource(String domain, String type, Set<String> fields) {
        this.resourceUri = new ResourceUri(domain, type);
        this.fields = fields;
    }

    public SearchResource(String domain, String type, String relation, Set<String> fields) {
        this.resourceUri = new ResourceUri(domain, type, null, relation);
        this.fields = fields;
    }

    @JsonIgnore
    public String getType() {
        return resourceUri.getType();
    }

    @JsonIgnore
    public void setType(String type) {
        resourceUri.setType(type);
    }

    @JsonIgnore
    public String getRelation() {
        return resourceUri.getRelation();
    }

    @JsonIgnore
    public void setRelation(String type) {
        resourceUri.setRelation(type);
    }

    public ResourceUri getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(ResourceUri resourceUri) {
        this.resourceUri = resourceUri;
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }


}
