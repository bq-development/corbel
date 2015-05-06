package com.bq.oss.corbel.eventbus;

public abstract class EventWithSpecificDomain implements Event {
    protected String domain;

    public EventWithSpecificDomain() {}

    public EventWithSpecificDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EventWithSpecificDomain))
            return false;

        EventWithSpecificDomain that = (EventWithSpecificDomain) o;

        if (domain != null ? !domain.equals(that.domain) : that.domain != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return domain != null ? domain.hashCode() : 0;
    }

}
