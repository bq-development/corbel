package io.corbel.evci.service;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.AmqpTemplate;

import io.corbel.evci.service.DefaultEventService;
import io.corbel.evci.service.EvciMQ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventServiceTest {

	private DefaultEventService eventService;
	@Mock
	private AmqpTemplate amqpTemplate;

	@Before
	public void setUp() {
		eventService = new DefaultEventService(amqpTemplate, type -> type.replace(":", "."));
	}

	@Test
	public void registerEventTest() throws JsonProcessingException, IOException {
		String type = "type";
		JsonNode event = new ObjectMapper().readTree("{\"a\":\"1\"}");

		eventService.registerEvent(type, event);

		Mockito.verify(amqpTemplate).convertAndSend(EvciMQ.EVENTS_EXCHANGE, type, event);
	}
}
