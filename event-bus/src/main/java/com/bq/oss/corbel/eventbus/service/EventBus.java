package com.bq.oss.corbel.eventbus.service;

import com.bq.oss.corbel.eventbus.Event;

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
