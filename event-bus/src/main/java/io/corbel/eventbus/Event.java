package io.corbel.eventbus;

/**
 * @author Francisco Sanchez
 */
public interface Event {
    default String getDomain() {
        return null;
    }
}
