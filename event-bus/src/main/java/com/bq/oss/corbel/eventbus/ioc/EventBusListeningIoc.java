package com.bq.oss.corbel.eventbus.ioc;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.bq.oss.corbel.eventbus.EventHandler;
import com.bq.oss.corbel.eventbus.rabbit.EventBusRabbitMQ;
import com.bq.oss.corbel.eventbus.service.DefaultIncomingEventProcessor;
import com.bq.oss.corbel.eventbus.service.EventBusRegistry;
import com.bq.oss.corbel.eventbus.service.InMemoryEventBusRegistry;
import com.bq.oss.corbel.eventbus.service.IncomingEventProcessor;
import com.bq.oss.lib.rabbitmq.config.AmqpConfiguration;
import com.bq.oss.lib.rabbitmq.config.BackoffOptions;

/**
 * This IoC loads a context which allow registering {@link EventHandler} in the event bus an start listening for events.
 * 
 * @author Rubén Carrasco
 * 
 */
@Configuration @Import({EventBusIoc.class}) public class EventBusListeningIoc {

    @Autowired private Environment env;

    @Bean
    public EventBusRegistry eventBusRegistry() {
        return new InMemoryEventBusRegistry();
    }

    @Bean
    public IncomingEventProcessor incomingEventProcessor() {
        return new DefaultIncomingEventProcessor(eventBusRegistry());
    }

    @Bean
    public EventBusRegistrar eventBusRegistrar() {
        return new EventBusRegistrar(eventBusRegistry());
    }

    @Bean
    public ExecutorService threadPoolExecutor() {
        final AtomicInteger threadCounter = new AtomicInteger(1);
        Integer threadsNumber = env.getProperty("eventbus.concurrency", Integer.class, Runtime.getRuntime().availableProcessors() * 2);
        return Executors.newFixedThreadPool(threadsNumber, runnable -> {
            return new Thread(runnable, "eventbus-thread-" + threadCounter.getAndIncrement());
        });
    }

    @Bean
    public AmqpConfiguration eventBusListenerRabbitMQConfiguration(MessageListenerAdapter messageListenerAdapter) {
        ExecutorService threadPoolExecutor = threadPoolExecutor();
        return configurer -> {
            String queueName = MessageFormat.format(EventBusRabbitMQ.EVENTNBUS_LISTENER_QUEUE_TEMPLATE,
                    env.getProperty("eventbus.listener.name"));
            configurer.bind(EventBusRabbitMQ.EVENTBUS_EXCHANGE, configurer.queue(queueName), Optional.empty(), Optional.empty());
            SimpleMessageListenerContainer container = configurer.listenerContainer(
                    threadPoolExecutor,
                    configurer.setRetryOpertations(Optional.ofNullable(env.getProperty("eventbus.maxAttempts", Integer.class)),
                            Optional.ofNullable(getBackoffOptions())), queueName);
            container.setQueueNames(queueName);
            container.setMessageListener(messageListenerAdapter);
            container.start();

        };
    }

    @Bean
    public MessageListenerAdapter getMessageListenerAdapter(MessageConverter messageConverter) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(incomingEventProcessor(), "process");
        messageListenerAdapter.setMessageConverter(messageConverter);
        return messageListenerAdapter;
    }

    private BackoffOptions getBackoffOptions() {
        Long intialInterval = env.getProperty("eventbus.backoff.initialInterval", Long.class);
        Double multiplier = env.getProperty("eventbus.backoff.multiplier", Double.class);
        Long maxInterval = env.getProperty("eventbus.backoff.maxInterval", Long.class);
        if (intialInterval == null || maxInterval == null || multiplier == null) {
            return null;
        }
        BackoffOptions backoffOptions = new BackoffOptions();
        backoffOptions.setInitialInterval(intialInterval);
        backoffOptions.setMultiplier(multiplier);
        backoffOptions.setMaxInterval(maxInterval);
        return backoffOptions;
    }
}
