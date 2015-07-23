package com.bq.oss.corbel.evci.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.Arrays;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bq.oss.corbel.evci.service.EventsService;
import io.corbel.lib.ws.gson.GsonMessageReaderWriterProvider;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;

public class EventResourceTest {

    private static final EventsService eventsService = mock(EventsService.class);

    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder().addProvider(new GsonMessageReaderWriterProvider())
            .addResource(new EventResource(eventsService)).build();

    @Before
    public void setUp() {
        reset(eventsService);
    }

    @Test
    public void testRegisterFormParamsEvent() {

        String type = "type:type";
        String dotType = "type.type";
        MultivaluedMap<String, String> event = new MultivaluedHashMap();
        event.add("key1", "value1");
        event.add("key2", "value2");
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request()
                .post(Entity.form(event), Response.class);

        Assert.assertEquals(202, response.getStatus());

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(eventsService).registerEvent(Mockito.eq(dotType), captor.capture());
        JsonNode json = captor.getValue();
        assertThat(json.get("key1").textValue()).isEqualTo("value1");
        assertThat(json.get("key2").textValue()).isEqualTo("value2");
    }

    @Test
    public void testRegisterFormParamsWithMultipleValueEvent() {

        String type = "type";
        MultivaluedMap<String, String> event = new MultivaluedHashMap();
        event.addAll("key1", Arrays.asList("value1", "value2"));
        String eventJson = "{\"key1\":[\"value1\",\"value2\"]}";
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request()
                .post(Entity.form(event), Response.class);

        Assert.assertEquals(202, response.getStatus());

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(eventsService).registerEvent(Mockito.eq(type), captor.capture());
        assertThat(captor.getValue().toString()).isEqualTo(eventJson);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testRegisterEmptyFormParamsEvent() throws Throwable {
        String type = "type";
        try {
            RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request().post(Entity.json(""), Response.class);
        } catch (ProcessingException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testRegisterEvent() {

        String type = "type";
        String eventJson = "{\"prop\":\"value\"}";

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request()
                .post(Entity.json(eventJson), Response.class);

        Assert.assertEquals(202, response.getStatus());

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(eventsService).registerEvent(Mockito.eq(type), captor.capture());
        assertThat(captor.getValue().toString()).isEqualTo(eventJson);
    }

    @Test(expected = JsonParseException.class)
    public void testRegisterInvalidEvent() throws Throwable {

        String type = "type";
        String event = "sdfsdf";
        try {
            RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request().post(Entity.json(event), Response.class);
        } catch (ProcessingException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testRegisterEmptyEvent() throws Throwable {
        String type = "type";
        try {
            RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request().post(Entity.json(""), Response.class);
        } catch (ProcessingException e) {
            throw e.getCause();
        }
    }

}
