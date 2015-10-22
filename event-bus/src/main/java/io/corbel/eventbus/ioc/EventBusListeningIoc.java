package io.corbel.eventbus.ioc;

import io.corbel.eventbus.EventHandler;
import io.corbel.eventbus.rabbit.EventBusRabbitMQ;
import io.corbel.eventbus.service.DefaultIncomingEventProcessor;
import io.corbel.eventbus.service.EventBusRegistry;
import io.corbel.eventbus.service.InMemoryEventBusRegistry;
import io.corbel.eventbus.service.IncomingEventProcessor;
import io.corbel.lib.rabbitmq.config.AmqpConfiguration;
import io.corbel.lib.rabbitmq.config.BackoffOptions;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
            if(!isConsoleMode()){
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
            }
        };
    }

    @Bean
    public MessageListenerAdapter getMessageListenerAdapter(MessageConverter messageConverter) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(incomingEventProcessor(), "process");
        messageListenerAdapter.setMessageConverter(messageConverter);
        return messageListenerAdapter;
    }

    private boolean isConsoleMode() {
        return Optional.ofNullable(System.getProperty("mode")).map(mode -> mode!=null && mode.equals("console")).orElse(false);
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
