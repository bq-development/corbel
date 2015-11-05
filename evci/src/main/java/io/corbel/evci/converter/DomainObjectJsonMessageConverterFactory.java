package io.corbel.evci.converter;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DomainObjectJsonMessageConverterFactory {

	private final ObjectMapper objectMapper;

	public DomainObjectJsonMessageConverterFactory(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public DomainObjectJsonMessageConverter createConverter(Type messageType) {
		return new DomainObjectJsonMessageConverter(messageType, objectMapper);
	}

}
