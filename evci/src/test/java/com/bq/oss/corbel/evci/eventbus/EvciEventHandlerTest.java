package com.bq.oss.corbel.evci.eventbus;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.evci.eventbus.EvciEventHandler;
import com.bq.oss.corbel.evci.service.EventsService;
import com.bq.oss.corbel.event.EvciEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Cristian del Cerro
 */

@RunWith(MockitoJUnitRunner.class)
public class EvciEventHandlerTest {

	private static final String TEST_TYPE = "testType";

	@Mock
	private EventsService eventsService;

	@Mock
	private ObjectMapper objectMapper;

	private EvciEventHandler evciEventHandler;

	@Before
	public void setUp() throws Exception {
		evciEventHandler = new EvciEventHandler(eventsService, objectMapper);
	}

	@Test
	public void testData() throws JsonProcessingException, IOException {
		EvciEvent evciEvent = new EvciEvent();
		evciEvent.setType(TEST_TYPE);
		String json = "{\"a\":1}";

		evciEvent.setData(json);

		JsonNode jsonNode = mock(JsonNode.class);

		when(objectMapper.readTree(Mockito.eq(json))).thenReturn(jsonNode);

		evciEventHandler.handle(evciEvent);
		verify(eventsService, times(1)).registerEvent(TEST_TYPE, jsonNode);
	}

	@Test
	public void testGetEventType() {
		assertThat(evciEventHandler.getEventType()).isEqualTo(EvciEvent.class);
	}

}
