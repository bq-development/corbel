package io.corbel.iam.model;

import java.util.Objects;

/**
 * An {@link Entity} is a object that have identity
 * 
 * @author Alexander De Leon
 * 
 */
public class Entity {

    private String id;

    public Entity(Entity entity) {
        id = entity.id;
    }

    public Entity(String id) {
        this.id = id;
    }

    public Entity() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Entity)) {
            return false;
        }
        Entity that = (Entity) obj;
        return Objects.equals(this.id, that.id);
    }
}
