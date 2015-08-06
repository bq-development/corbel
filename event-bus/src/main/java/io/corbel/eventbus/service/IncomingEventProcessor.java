package io.corbel.eventbus.service;

import io.corbel.eventbus.Event;

/**
 * @author Francisco Sanchez
 */
public interface IncomingEventProcessor {

    public void process(Event event);
}
