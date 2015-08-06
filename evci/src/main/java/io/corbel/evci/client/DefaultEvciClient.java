package io.corbel.evci.client;

import io.corbel.evci.service.EventsService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alexander De Leon
 *
 */
public class DefaultEvciClient implements EvciClient {

    private final EventsService service;
    private final ObjectMapper objectMapper;

    public DefaultEvciClient(EventsService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @Override
    public void convertAndSend(String type, Object event) {
        service.registerEvent(type, objectMapper.valueToTree(event));
    }

}
