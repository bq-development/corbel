package com.bq.oss.corbel.eventbus.ioc;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.bq.oss.corbel.eventbus.rabbit.EventBusRabbitMQ;
import com.bq.oss.lib.rabbitmq.config.AmqpConfiguration;
import com.bq.oss.lib.rabbitmq.ioc.AbstractRabbitMQConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import java.util.Optional;

/**
 * @author Alberto J. Rubio
 *
 */
@Configuration public class RabbitEventBusIoc extends AbstractRabbitMQConfiguration {

    @Autowired private Environment env;

    @Bean(name = "eventBusAmqpTemplate")
    @Override
    public AmqpTemplate amqpTemplate() {
        return super.amqpTemplate();
    }

    @Bean
    public AmqpConfiguration eventBusRabbitMQConfiguration() {
        return configurer -> {
            configurer.fanoutExchange(EventBusRabbitMQ.EVENTBUS_EXCHANGE);
        };
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Bean
    public MessageConverter getDefaultMessageConverter() {
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setJsonObjectMapper(getObjectMapper());
        return jackson2JsonMessageConverter;
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

    @Override
    protected MessageConverter getMessageConverter() {
        return getDefaultMessageConverter();
    }


    @Override
    protected Optional<String> configPrefix() {
        return Optional.of("eventbus");
    }


}
