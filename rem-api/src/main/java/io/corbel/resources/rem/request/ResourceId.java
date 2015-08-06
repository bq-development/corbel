package io.corbel.resources.rem.request;

/**
 * @author Alberto J. Rubio
 *
 */
public class ResourceId {

    public static final String WILDCARD_RESOURCE_ID = "_";

    private String id;

    public ResourceId(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isWildcard() {
        return WILDCARD_RESOURCE_ID.equals(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ResourceId))
            return false;

        ResourceId that = (ResourceId) o;

        if (!id.equals(that.id))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
