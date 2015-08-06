package io.corbel.eventbus.service;

import io.corbel.eventbus.Event;

/**
 * @author Francisco Sanchez
 */
public interface EventBus {

    /**
     * Dispatch (submit) a new event to the event bus.
     * 
     * @param event the event to dispatch.
     */
    void dispatch(Event event);

}
