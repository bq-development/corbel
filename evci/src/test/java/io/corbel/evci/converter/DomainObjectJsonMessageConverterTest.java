package io.corbel.evci.converter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DomainObjectJsonMessageConverterTest {

	private static final String TEST_ID = "_id";
	private static final String TEST_MESSAGE = "_message";
	private DomainObjectJsonMessageConverter converter;

	@Before
	public void setup() {
		converter = new DomainObjectJsonMessageConverter(TestDomainObject.class, new ObjectMapper());
	}

	@Test
	public void testConvertToObject() {
		String json = "{\"id\":\"" + TEST_ID + "\", \"message\":\"" + TEST_MESSAGE + "\"}";

		MessageProperties properties = new MessageProperties();
		properties.setContentType("application/json");
		Message message = new Message(json.getBytes(), properties);
		TestDomainObject object = (TestDomainObject) converter.fromMessage(message);

		Assert.assertEquals(TEST_ID, object.id);
		Assert.assertEquals(TEST_MESSAGE, object.message);
	}

	@Test(expected = MessageConversionException.class)
	public void testConvertNoJsonHeader() {
		String json = "{\"id\":\"" + TEST_ID + "\", \"message\":\"" + TEST_MESSAGE + "\"}";

		MessageProperties properties = new MessageProperties();
		Message message = new Message(json.getBytes(), properties);
		converter.fromMessage(message);
	}

	public static class TestDomainObject {
		private String id;
		private String message;

		public String getId() {
			return id;
		}

		public TestDomainObject setId(String id) {
			this.id = id;
			return this;
		}

		public String getMessage() {
			return message;
		}

		public TestDomainObject setMessage(String message) {
			this.message = message;
			return this;
		}

	}

}
