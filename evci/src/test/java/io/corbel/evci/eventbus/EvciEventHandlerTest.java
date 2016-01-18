package io.corbel.evci.eventbus;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.corbel.evci.service.EventsService;
import io.corbel.event.EvciEvent;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Cristian del Cerro
 */

@RunWith(MockitoJUnitRunner.class) public class EvciEventHandlerTest {

    private static final String TEST_TYPE = "testType";
    private static final String TEST_DOMAIN = "domain";

    @Mock private EventsService eventsService;

    private ObjectMapper objectMapper;

    private EvciEventHandler evciEventHandler;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        evciEventHandler = new EvciEventHandler(eventsService, objectMapper);
    }

    @Test
    public void testData() throws JsonProcessingException, IOException {
        EvciEvent evciEvent = new EvciEvent();
        evciEvent.setType(TEST_TYPE);
        evciEvent.setDomain(TEST_DOMAIN);
        String json = "{\"a\":1}";

        evciEvent.setData(json);

        ArgumentCaptor<JsonNode> jsonNode = ArgumentCaptor.forClass(JsonNode.class);
        evciEventHandler.handle(evciEvent);
        verify(eventsService, times(1)).registerEvent(eq(TEST_TYPE), jsonNode.capture());

        assertThat(jsonNode.getValue().path("content").path("a").asInt()).isEqualTo(1);
        assertThat(jsonNode.getValue().path("header").path("domainId").asText()).isEqualTo(TEST_DOMAIN);
    }

    @Test
    public void testGetEventType() {
        assertThat(evciEventHandler.getEventType()).isEqualTo(EvciEvent.class);
    }

}
