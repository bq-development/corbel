package com.bq.oss.corbel.iam.eventbus;

import com.bq.oss.corbel.event.DomainDeletedEvent;
import com.bq.oss.corbel.eventbus.EventHandler;
import com.bq.oss.corbel.iam.repository.ClientRepository;
import com.bq.oss.corbel.iam.repository.UserRepository;

public class DomainDeletedEventHandler implements EventHandler<DomainDeletedEvent> {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public DomainDeletedEventHandler(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(DomainDeletedEvent event) {
        String domain = event.getDomain();
        clientRepository.deleteByDomain(domain);
        userRepository.deleteByDomain(domain);
    }

    @Override
    public Class<DomainDeletedEvent> getEventType() {
        return DomainDeletedEvent.class;
    }
}
