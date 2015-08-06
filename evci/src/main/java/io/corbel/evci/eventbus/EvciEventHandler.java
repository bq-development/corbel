package io.corbel.evci.eventbus;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.evci.eventbus.EvciEventHandler;
import io.corbel.evci.service.EventsService;
import io.corbel.event.EvciEvent;
import io.corbel.eventbus.EventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Cristian del Cerro
 */
public class EvciEventHandler implements EventHandler<EvciEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(EvciEventHandler.class);

	private final EventsService eventsService;
	private final ObjectMapper objectMapper;

	public EvciEventHandler(EventsService eventsService, ObjectMapper objectMapper) {
		this.eventsService = eventsService;
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(EvciEvent evciEvent) {
		Object data = evciEvent.getData();
		if (Objects.nonNull(data)) {
			try {
				eventsService.registerEvent(evciEvent.getType(), objectMapper.readTree(evciEvent.getData()));
			} catch (Exception e) {
				LOG.error("Received EvciEvent with unparsable JSON data.", e);
				throw new RuntimeException(e); // causes message to be rejected and send to dead-letter queue
			}
		} else {
			LOG.warn("Received EvciEvent with null data. Ignoring event!");
		}
	}

	@Override
	public Class<EvciEvent> getEventType() {
		return EvciEvent.class;
	}
}
