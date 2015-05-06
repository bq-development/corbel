package com.bq.oss.corbel.eventbus.service;

import com.bq.oss.corbel.eventbus.Event;

/**
 * @author Francisco Sanchez
 */
public interface IncomingEventProcessor {

    public void process(Event event);
}
