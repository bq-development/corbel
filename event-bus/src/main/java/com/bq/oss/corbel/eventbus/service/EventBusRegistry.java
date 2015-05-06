package com.bq.oss.corbel.eventbus.service;

import com.bq.oss.corbel.eventbus.Event;
import com.bq.oss.corbel.eventbus.EventHandler;

/**
 * @author Alexander De Leon
 *
 */
public interface EventBusRegistry {

    /**
     * Register a {@link EventHandler} with the event bus.
     * 
     * @param eventHandler
     */
    <E extends Event> void register(EventHandler<E> eventHandler);

    /**
     * Get all registered event handlers for a specified event type.
     * 
     * @param eventType
     * @return an iterator with the registered event handlers. It will never return null.
     */
    Iterable<EventHandler<? extends Event>> getHandlers(Class<? extends Event> eventType);

}
