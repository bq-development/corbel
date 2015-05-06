package com.bq.oss.corbel.eventbus;

/**
 * @author Francisco Sanchez
 */
public interface EventHandler<T extends Event> {

    public void handle(T event);

    public Class<T> getEventType();

}
