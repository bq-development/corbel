package io.corbel.eworker.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import io.corbel.evci.converter.DomainObjectJsonMessageConverterFactory;
import io.corbel.evci.eworker.Eworker;
import io.corbel.evci.eworker.EworkerRegistry;
import io.corbel.evci.service.EvciMQ;
import io.corbel.lib.rabbitmq.config.AmqpConfigurer;
import io.corbel.lib.rabbitmq.config.BackoffOptions;

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
    public <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, boolean handleFailures, int threadsNumber) {
        Type eworkerType = ((ParameterizedType) eworker.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        String queueName = "evci.eworker." + queue + ".queue";
        String deadLetterQueueName = "evci.eworker." + queue + ".dlq";
        configurer.bind(EvciMQ.EVENTS_EXCHANGE,
                configurer.queue(queueName, configurer.setDeadLetterExchange(EvciMQ.EVENTS_DEAD_LETTER_EXCHANGE)),
                Optional.of(routingPatternFunction.apply(routingPattern)), Optional.<Map<String, Object>>empty());

        // Configure dead letter queue
        configurer.bind(EvciMQ.EVENTS_DEAD_LETTER_EXCHANGE, configurer.queue(deadLetterQueueName),
                Optional.of(routingPatternFunction.apply(routingPattern)), Optional.<Map<String, Object>>empty());

        MessageListenerAdapter messageListener = new MessageListenerAdapter(eworker, converterFactory.createConverter(eworkerType));

        SimpleMessageListenerContainer container = configurer.listenerContainer(Executors.newFixedThreadPool(threadsNumber),
                configurer.setRetryOpertations(Optional.of(maxAttempts), Optional.ofNullable(backoffOptions)), queueName);

        container.setMessageListener(messageListener);

        if (handleFailures) {
            MessageListenerAdapter failedMessageListener = new MessageListenerAdapter(eworker,
                    converterFactory.createConverter(eworkerType));
            failedMessageListener.setDefaultListenerMethod("handleFailedMessage");
            SimpleMessageListenerContainer faildMessageContainer = configurer.listenerContainer(Executors.newFixedThreadPool(threadsNumber),
                    deadLetterQueueName);
            faildMessageContainer.setMessageListener(failedMessageListener);
            faildMessageContainer.start(); // Start listening for failures
        }

        container.start(); // Start listening for messages
    }
}
