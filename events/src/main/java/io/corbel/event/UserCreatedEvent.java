package io.corbel.event;

import io.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Alberto J. Rubio
 */
public class UserCreatedEvent extends EventWithSpecificDomain {
    private String userId;
    private String email;
    private String country;

    public UserCreatedEvent() {}

    public UserCreatedEvent(String userId, String domainId, String email, String country) {
        super(domainId);
        this.userId = userId;
        this.email = email;
        this.country = country;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserCreatedEvent))
            return false;
        if (!super.equals(o))
            return false;

        UserCreatedEvent that = (UserCreatedEvent) o;

        if (country != null ? !country.equals(that.country) : that.country != null)
            return false;
        if (email != null ? !email.equals(that.email) : that.email != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}
