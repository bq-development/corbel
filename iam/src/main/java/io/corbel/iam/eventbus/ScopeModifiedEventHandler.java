package io.corbel.iam.eventbus;

import io.corbel.event.ScopeUpdateEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.iam.repository.ScopeRepository;

import org.springframework.cache.CacheManager;

public class ScopeModifiedEventHandler implements EventHandler<ScopeUpdateEvent> {
    private final CacheManager cacheManager;


    public ScopeModifiedEventHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(ScopeUpdateEvent event) {
        cacheManager.getCache(ScopeRepository.SCOPE_CACHE).evict(event.getScopeId());
    }

    @Override
    public Class<ScopeUpdateEvent> getEventType() {
        return ScopeUpdateEvent.class;
    }
}
