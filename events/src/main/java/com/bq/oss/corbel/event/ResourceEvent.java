package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Alberto J. Rubio
 */
public class ResourceEvent extends EventWithSpecificDomain {

    public enum Action {
        CREATE, UPDATE, DELETE
    }

    private String type;
    private String resourceId;
    private Action action;

    private ResourceEvent() {}

    private ResourceEvent(String type, String resourceId, String domain, Action action) {
        super(domain);
        this.type = type;
        this.resourceId = resourceId;
        this.action = action;
    }

    public static ResourceEvent createResourceEvent(String type, String resourceId, String domain) {
        return new ResourceEvent(type, resourceId, domain, Action.CREATE);
    }

    public static ResourceEvent updateResourceEvent(String type, String resourceId, String domain) {
        return new ResourceEvent(type, resourceId, domain, Action.UPDATE);
    }

    public static ResourceEvent deleteResourceEvent(String type, String resourceId, String domain) {
        return new ResourceEvent(type, resourceId, domain, Action.DELETE);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResourceEvent other = (ResourceEvent) obj;
        if (action != other.action) {
            return false;
        }
        if (resourceId == null) {
            if (other.resourceId != null) {
                return false;
            }
        } else if (!resourceId.equals(other.resourceId)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

}
