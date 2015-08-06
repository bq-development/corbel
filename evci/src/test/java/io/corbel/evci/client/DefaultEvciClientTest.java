package io.corbel.evci.client;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.corbel.evci.service.EventsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alexander De Leon
 *
 */
public class DefaultEvciClientTest {

    private static final String TEST_TYPE = "test:type";
    private EventsService serviceMock;
    private EvciClient client;

    @Before
    public void setup() {
        serviceMock = mock(EventsService.class);
        client = new DefaultEvciClient(serviceMock, new ObjectMapper());
    }

    @Test
    public void test() {
        TestBean event = new TestBean();
        event.num = 1;
        event.str = "a";
        client.convertAndSend(TEST_TYPE, event);
        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(serviceMock).registerEvent(Mockito.eq(TEST_TYPE), eventCaptor.capture());
        assertThat(eventCaptor.getValue().toString()).isEqualTo("{\"num\":1,\"str\":\"a\"}");
    }

    public static class TestBean {
        public int num;
        public String str;
    }

}
