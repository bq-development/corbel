package io.corbel.evci.client;

/**
 * @author Alexander De Leon
 *
 */
public interface EvciClient {

    /**
     * Internally send a EVCI event
     * 
     * @param type the type of the event (e.g. music:Play)
     * @param event The java object to be serialized into the JSON event data
     */
    void convertAndSend(String type, Object event);

}
