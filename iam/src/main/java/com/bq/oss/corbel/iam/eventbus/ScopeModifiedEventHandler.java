package com.bq.oss.corbel.iam.eventbus;

import com.bq.oss.corbel.event.ScopeUpdateEvent;
import com.bq.oss.corbel.eventbus.EventHandler;
import com.bq.oss.corbel.iam.repository.ScopeRepository;
import com.bq.oss.corbel.iam.service.DefaultScopeService;
import org.springframework.cache.CacheManager;

public class ScopeModifiedEventHandler implements EventHandler<ScopeUpdateEvent> {
    private final CacheManager cacheManager;


    public ScopeModifiedEventHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(ScopeUpdateEvent event) {
        cacheManager.getCache(DefaultScopeService.EXPAND_SCOPES_CACHE).clear();
        cacheManager.getCache(ScopeRepository.SCOPE_CACHE).evict(event.getScopeId());
    }

    @Override
    public Class<ScopeUpdateEvent> getEventType() {
        return ScopeUpdateEvent.class;
    }
}
