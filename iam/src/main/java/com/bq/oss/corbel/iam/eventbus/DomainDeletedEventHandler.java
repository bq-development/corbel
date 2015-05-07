package com.bq.oss.corbel.iam.eventbus;

import com.bq.oss.corbel.event.DomainDeletedEvent;
import com.bq.oss.corbel.eventbus.EventHandler;
import com.bq.oss.corbel.iam.repository.ClientRepository;

public class DomainDeletedEventHandler implements EventHandler<DomainDeletedEvent> {
    private final ClientRepository clientRepository;

    public DomainDeletedEventHandler(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void handle(DomainDeletedEvent event) {
        clientRepository.deleteByDomain(event.getDomain());
    }

    @Override
    public Class<DomainDeletedEvent> getEventType() {
        return DomainDeletedEvent.class;
    }
}
