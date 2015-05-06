package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

/**
 * @author Alberto J. Rubio
 */
public class PaymentEvent extends EventWithSpecificDomain {

    public enum Type {
        NEW_PAYMENT_PLAN, SUCCESS_RECURRING_PAYMENT, SUCCESS_PAYMENT, RECURRING_PAYMENT_FAILURE, PAYMENT_PLAN_TERMINATED, PAYMENT_METHOD_UPDATE_IN_PAYMENT_PLAN
    }

    private String userId;
    private String resourceId;
    private Type type;

    public PaymentEvent() {}

    public PaymentEvent(String userId, String domainId, String resourceId, Type type) {
        super(domainId);
        this.userId = userId;
        this.resourceId = resourceId;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PaymentEvent))
            return false;
        if (!super.equals(o))
            return false;

        PaymentEvent that = (PaymentEvent) o;

        if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null)
            return false;
        if (type != that.type)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
