package io.corbel.resources.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;

/**
 * @author Rubén Carrasco
 * 
 */
public class RelationSchema {

    @Id private String type;

    private Map<String, Set<String>> relations;

    public RelationSchema() {
        relations = new HashMap<String, Set<String>>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Set<String>> getRelations() {
        return relations;
    }

    public void setRelations(Map<String, Set<String>> relations) {
        this.relations = relations;
    }

}
