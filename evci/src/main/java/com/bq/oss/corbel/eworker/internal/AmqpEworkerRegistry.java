package com.bq.oss.corbel.eworker.internal;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import com.bq.oss.corbel.evci.eworker.Eworker;
import com.bq.oss.corbel.evci.eworker.EworkerRegistry;
import com.bq.oss.corbel.evci.service.EvciMQ;
import com.bq.oss.lib.rabbitmq.config.AmqpConfigurer;
import com.bq.oss.lib.rabbitmq.config.BackoffOptions;
import com.bq.oss.lib.rabbitmq.converter.DomainObjectJsonMessageConverterFactory;

/**
 * Created by Alberto J. Rubio
 */
public class AmqpEworkerRegistry implements EworkerRegistry {

    private final DomainObjectJsonMessageConverterFactory converterFactory;
    private final AmqpConfigurer configurer;

    private final BackoffOptions backoffOptions;
    private final int maxAttempts;
    private final UnaryOperator<String> routingPatternFunction;

    public AmqpEworkerRegistry(DomainObjectJsonMessageConverterFactory converterFactory, AmqpConfigurer configurer,
            BackoffOptions backoffOptions, int maxAttempts, UnaryOperator<String> routingPatternFunction) {
        super();
        this.converterFactory = converterFactory;
        this.configurer = configurer;
        this.backoffOptions = backoffOptions;
        this.maxAttempts = maxAttempts;
        this.routingPatternFunction = routingPatternFunction;
    }

    @Override
    public <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, Class<E> messageType, boolean handleFailures,
            int threadsNumber) {
        registerEworker(eworker, routingPattern, queue, (Type) messageType, handleFailures, threadsNumber);
    }

    @Override
    public void registerEworker(Eworker<?> eworker, String routingPattern, String queue, Type messageType, boolean handleFailures,
            int threadsNumber) {
        String queueName = "evci.eworker." + queue + ".queue";
        String deadLetterQueueName = "evci.eworker." + queue + ".dlq";
        configurer.bind(EvciMQ.EVENTS_EXCHANGE,
                configurer.queue(queueName, configurer.setDeadLetterExchange(EvciMQ.EVENTS_DEAD_LETTER_EXCHANGE)),
                Optional.of(routingPatternFunction.apply(routingPattern)), Optional.<Map<String, Object>>empty());

        // configure dead letter queue
        configurer.bind(EvciMQ.EVENTS_DEAD_LETTER_EXCHANGE, configurer.queue(deadLetterQueueName),
                Optional.of(routingPatternFunction.apply(routingPattern)), Optional.<Map<String, Object>>empty());

        MessageListenerAdapter messageListener = new MessageListenerAdapter(eworker, converterFactory.createConverter(messageType));

        SimpleMessageListenerContainer container = configurer.listenerContainer(Executors.newFixedThreadPool(threadsNumber),
                configurer.setRetryOpertations(Optional.of(maxAttempts), Optional.ofNullable(backoffOptions)), queueName);

        container.setMessageListener(messageListener);

        if (handleFailures) {
            MessageListenerAdapter failedMessageListener = new MessageListenerAdapter(eworker,
                    converterFactory.createConverter(messageType));
            failedMessageListener.setDefaultListenerMethod("handleFailedMessage");

            SimpleMessageListenerContainer faildMessageContainer = configurer.listenerContainer(
                    Executors.newFixedThreadPool(threadsNumber), deadLetterQueueName);

            faildMessageContainer.setMessageListener(failedMessageListener);
            faildMessageContainer.start(); // start listening for failures
        }

        container.start(); // start listening for messages
    }
}
