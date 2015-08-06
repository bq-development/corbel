package io.corbel.eventbus.ioc;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.corbel.eventbus.service.EventBus;
import io.corbel.eventbus.service.RabbitEventBus;

/**
 * @author Francisco Sanchez
 */
@Configuration @Import({RabbitEventBusIoc.class}) public class EventBusIoc {

    @Autowired private Environment env;

    @Autowired @Qualifier("eventBusAmqpTemplate") private AmqpTemplate amqpTemplate;

    @Bean
    public EventBus getEventBus() {
        return new RabbitEventBus(amqpTemplate, env.getProperty("eventbus.exchange"));
    }
}
