package com.bq.oss.corbel.evci.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.bq.oss.corbel.evci.api.ApiVersion;
import com.bq.oss.corbel.evci.api.EventResource;
import com.bq.oss.corbel.evci.service.EventsService;
import com.bq.oss.lib.ws.gson.GsonMessageReaderWriterProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.MappableContainerException;

import io.dropwizard.testing.junit.ResourceTestRule;

public class EventResourceTest {

	private static final EventsService eventsService = mock(EventsService.class);

	@ClassRule
	public static ResourceTestRule RULE = ResourceTestRule.builder().addProvider(new GsonMessageReaderWriterProvider())
			.addResource(new EventResource(eventsService)).build();

	@Before
	public void setUp() {
		reset(eventsService);
	}

	@Test
	public void testRegisterFormParamsEvent() {

		String type = "type:type";
		String dotType = "type.type";
		String event = "key1=value1&key2=value2";
		ClientResponse response = RULE.client().resource("/" + ApiVersion.CURRENT + "/event/" + type)
				.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, event);

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
		String event = "key1=value1&key1=value2";
		String eventJson = "{\"key1\":[\"value1\",\"value2\"]}";
		ClientResponse response = RULE.client().resource("/" + ApiVersion.CURRENT + "/event/" + type)
				.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, event);

		Assert.assertEquals(202, response.getStatus());

		ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
		Mockito.verify(eventsService).registerEvent(Mockito.eq(type), captor.capture());
		assertThat(captor.getValue().toString()).isEqualTo(eventJson);
	}

	@Test(expected = ConstraintViolationException.class)
	public void testRegisterEmptyFormParamsEvent() {
		String type = "type";

		RULE.client().resource("/" + ApiVersion.CURRENT + "/event/" + type)
				.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class);
	}

	@Test
	public void testRegisterEvent() {

		String type = "type";
		String eventJson = "{\"prop\":\"value\"}";

		ClientResponse response = RULE.client().resource("/" + ApiVersion.CURRENT + "/event/" + type)
				.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, eventJson);

		Assert.assertEquals(202, response.getStatus());

		ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
		Mockito.verify(eventsService).registerEvent(Mockito.eq(type), captor.capture());
		assertThat(captor.getValue().toString()).isEqualTo(eventJson);
	}

	@Test(expected = MappableContainerException.class)
	public void testRegisterInvalidEvent() {

		String type = "type";
		String event = "sdfsdf";

		RULE.client().resource("/" + ApiVersion.CURRENT + "/event/" + type).type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, event);

	}

	@Test(expected = ConstraintViolationException.class)
	public void testRegisterEmptyEvent() {
		String type = "type";

		RULE.client().resource("/" + ApiVersion.CURRENT + "/event/" + type)
				.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class);
	}

}
