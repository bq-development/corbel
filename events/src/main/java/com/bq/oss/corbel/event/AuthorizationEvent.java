package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.Event;
import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Rubén Carrasco
 *
 */
public class AuthorizationEvent extends EventWithSpecificDomain {

    private final IssuerType issuerType;
    private final String id;

    private AuthorizationEvent(String domainId, IssuerType issuerType, String id) {
        super(domainId);
        this.issuerType = issuerType;
        this.id = id;
    }

    public IssuerType getIssuerType() {
        return issuerType;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((issuerType == null) ? 0 : issuerType.hashCode());
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
        AuthorizationEvent other = (AuthorizationEvent) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (issuerType == null) {
            if (other.issuerType != null) {
                return false;
            }
        } else if (!issuerType.equals(other.issuerType)) {
            return false;
        }
        return true;
    }

    public static Event userAuthenticationEvent(String domainId, String id) {
        return new AuthorizationEvent(domainId, IssuerType.USER, id);
    }

    public static Event clientAuthenticationEvent(String domainId, String id) {
        return new AuthorizationEvent(domainId, IssuerType.CLIENT, id);
    }

    public enum IssuerType {
        USER, CLIENT
    }

}
