package io.corbel.iam.eventbus;

import io.corbel.event.ScopeUpdateEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.iam.repository.GroupRepository;
import io.corbel.iam.repository.ScopeRepository;

import org.springframework.cache.CacheManager;

public class ScopeModifiedEventHandler implements EventHandler<ScopeUpdateEvent> {
    private final CacheManager cacheManager;
    private final GroupRepository groupRepository;


    public ScopeModifiedEventHandler(CacheManager cacheManager, GroupRepository groupRepository) {
        this.cacheManager = cacheManager;
        this.groupRepository = groupRepository;
    }

    @Override
    public void handle(ScopeUpdateEvent event) {
        cacheManager.getCache(ScopeRepository.SCOPE_CACHE).evict(event.getScopeId());
        if( ScopeUpdateEvent.Action.DELETE.equals(event.getAction())){
            groupRepository.deleteScopes(event.getScopeId());
        }
    }

    @Override
    public Class<ScopeUpdateEvent> getEventType() {
        return ScopeUpdateEvent.class;
    }
}
