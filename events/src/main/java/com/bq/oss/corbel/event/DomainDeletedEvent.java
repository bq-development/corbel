package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

public class DomainDeletedEvent extends EventWithSpecificDomain {
    public DomainDeletedEvent(String domain) {
        super(domain);
    }
}
