package io.corbel.event;

import io.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Alberto J. Rubio
 */
public class LoanableResourceEvent extends EventWithSpecificDomain {

    private String resourceId;
    private boolean available;

    public LoanableResourceEvent() {}

    public LoanableResourceEvent(String domainId, String resourceId, boolean available) {
        super(domainId);
        this.resourceId = resourceId;
        this.available = available;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        LoanableResourceEvent that = (LoanableResourceEvent) o;

        if (available != that.available)
            return false;
        if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
        result = 31 * result + (available ? 1 : 0);
        return result;
    }
}
