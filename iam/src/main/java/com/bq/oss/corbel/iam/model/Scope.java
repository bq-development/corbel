package com.bq.oss.corbel.iam.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import com.bq.oss.lib.ws.json.serialization.JsonArrayToSetDeserializer;
import com.bq.oss.lib.ws.json.serialization.JsonObjectDeserializer;
import com.bq.oss.lib.ws.json.serialization.JsonObjectSerializer;
import com.bq.oss.lib.ws.json.serialization.JsonObjectSetToJsonArraySerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.JsonObject;

/**
 * @author Alexander De Leon
 * 
 */
public class Scope extends Entity {

    public static final String COMPOSITE_SCOPE_TYPE = "composite_scope";

    private String type;
    @NotEmpty private String audience;
    @NotEmpty @JsonDeserialize(using = JsonArrayToSetDeserializer.class) private Set<JsonObject> rules = new HashSet<>();
    private Set<String> scopes = new HashSet<>();
    @JsonDeserialize(using = JsonObjectDeserializer.class) private JsonObject parameters;

    @JsonIgnore
    public boolean isComposed() {
        return COMPOSITE_SCOPE_TYPE.equals(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    @JsonSerialize(using = JsonObjectSetToJsonArraySerializer.class)
    public Set<JsonObject> getRules() {
        return rules;
    }

    public void setRules(Set<JsonObject> rules) {
        this.rules = rules;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @JsonSerialize(using = JsonObjectSerializer.class)
    public JsonObject getParameters() {
        return parameters;
    }

    public void setParameters(JsonObject parameters) {
        this.parameters = parameters;
    }

    @JsonIgnore
    public String getIdWithParameters() {
        return getId()
                + Optional
                        .ofNullable(getParameters())
                        .map(parameters -> parameters.entrySet().stream()
                                .map(entry -> ";" + entry.getKey() + "=" + entry.getValue().getAsString()).collect(Collectors.joining()))
                        .orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        Scope scope = (Scope) o;

        if (audience != null ? !audience.equals(scope.audience) : scope.audience != null)
            return false;
        if (parameters != null ? !parameters.equals(scope.parameters) : scope.parameters != null)
            return false;
        if (rules != null ? !rules.equals(scope.rules) : scope.rules != null)
            return false;
        if (scopes != null ? !scopes.equals(scope.scopes) : scope.scopes != null)
            return false;
        if (type != null ? !type.equals(scope.type) : scope.type != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (audience != null ? audience.hashCode() : 0);
        result = 31 * result + (rules != null ? rules.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }



}
