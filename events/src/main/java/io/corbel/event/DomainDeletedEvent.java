package io.corbel.event;

import io.corbel.eventbus.EventWithSpecificDomain;

public class DomainDeletedEvent extends EventWithSpecificDomain {

    public DomainDeletedEvent() {}

    public DomainDeletedEvent(String domain) {
        super(domain);
    }

}
