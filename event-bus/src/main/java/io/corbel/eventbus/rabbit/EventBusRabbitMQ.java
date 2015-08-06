package io.corbel.eventbus.rabbit;

/**
 * @author Alexander De Leon
 *
 */
public interface EventBusRabbitMQ {

    String EVENTBUS_EXCHANGE = "eventbus.exchange";

    String EVENTNBUS_LISTENER_QUEUE_TEMPLATE = "eventbus.{0}.queue";
}
