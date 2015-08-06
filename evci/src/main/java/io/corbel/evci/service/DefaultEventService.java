package io.corbel.evci.service;

import java.util.function.UnaryOperator;

import org.springframework.amqp.core.AmqpTemplate;

import com.fasterxml.jackson.databind.JsonNode;

public class DefaultEventService implements EventsService {

	private final AmqpTemplate amqpTemplate;
	private final UnaryOperator<String> routingKeyFunction;

	public DefaultEventService(AmqpTemplate amqpTemplate, UnaryOperator<String> routingKeyFunction) {
		this.amqpTemplate = amqpTemplate;
		this.routingKeyFunction = routingKeyFunction;
	}

	@Override
	public void registerEvent(String type, JsonNode event) {
		amqpTemplate.convertAndSend(EvciMQ.EVENTS_EXCHANGE, routingKeyFunction.apply(type), event);
	}

}
