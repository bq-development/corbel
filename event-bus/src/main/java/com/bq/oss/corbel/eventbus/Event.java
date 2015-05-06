package com.bq.oss.corbel.eventbus;

/**
 * @author Francisco Sanchez
 */
public interface Event {
    default String getDomain() {
        return null;
    }
}
