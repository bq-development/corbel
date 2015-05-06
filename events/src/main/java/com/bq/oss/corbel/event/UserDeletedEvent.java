package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Cristian del Cerro
 */
public class UserDeletedEvent extends EventWithSpecificDomain {
    private String userId;

    public UserDeletedEvent() {}

    public UserDeletedEvent(String userId, String domain) {
        super(domain);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserDeletedEvent))
            return false;
        if (!super.equals(o))
            return false;

        UserDeletedEvent that = (UserDeletedEvent) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}
