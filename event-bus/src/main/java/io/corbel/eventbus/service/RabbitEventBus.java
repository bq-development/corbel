package io.corbel.eventbus.service;

import org.springframework.amqp.core.AmqpTemplate;

import io.corbel.eventbus.Event;

/**
 * @author Francisco Sanchez
 */
public class RabbitEventBus implements EventBus {
    private final AmqpTemplate amqpTemplate;
    private final String eventBusExchange;

    public RabbitEventBus(AmqpTemplate amqpTemplate, String eventBusExchange) {
        this.amqpTemplate = amqpTemplate;
        this.eventBusExchange = eventBusExchange;
    }

    @Override
    public void dispatch(Event event) {
        amqpTemplate.convertAndSend(eventBusExchange, event.getClass().toString(), event);
    }
}
