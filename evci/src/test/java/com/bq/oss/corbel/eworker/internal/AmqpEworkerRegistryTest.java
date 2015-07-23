package com.bq.oss.corbel.eworker.internal;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import com.bq.oss.corbel.evci.eworker.Eworker;
import io.corbel.lib.rabbitmq.config.AmqpConfigurer;
import io.corbel.lib.rabbitmq.config.RabbitMQConfigurer;
import io.corbel.lib.rabbitmq.converter.DomainObjectJsonMessageConverterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Alexander De Leon
 *
 */
public class AmqpEworkerRegistryTest {

    private RabbitAdmin rabbitAdminMock;
    private ConnectionFactory connectionFactoryMock;
    private AmqpEworkerRegistry registry;
    private AmqpConfigurer configurer;

    private Eworker<?> eworker;
    private final String QUEUE_TEST = "test";
    private final String ROUTING_PATTERN_TEST = "test.routing.pattern";

    @Before
    public void setup() {
        rabbitAdminMock = mock(RabbitAdmin.class);
        connectionFactoryMock = mock(ConnectionFactory.class);
        configurer = new RabbitMQConfigurer(rabbitAdminMock, connectionFactoryMock);

        DomainObjectJsonMessageConverterFactory converterFactory = new DomainObjectJsonMessageConverterFactory(new ObjectMapper());
        registry = new AmqpEworkerRegistry(converterFactory, configurer, null, 10, type -> type.replace(":", "."));
        eworker = mock(Eworker.class);
    }

    // Test TODO JACKSON
    public void testDeclareEworker() {
        ArgumentCaptor<Queue> evciQueueCaptor = ArgumentCaptor.forClass(Queue.class);
        ArgumentCaptor<Binding> bindingCaptor = ArgumentCaptor.forClass(Binding.class);

        registry.registerEworker(eworker, ROUTING_PATTERN_TEST, QUEUE_TEST, Object.class);
        verify(rabbitAdminMock, times(2)).declareQueue(evciQueueCaptor.capture());
        verify(rabbitAdminMock, times(2)).declareBinding(bindingCaptor.capture());

        assertThat(evciQueueCaptor.getAllValues().get(0).getName()).isEqualTo("evci.eworker." + QUEUE_TEST + ".queue");
        assertThat(evciQueueCaptor.getAllValues().get(1).getName()).isEqualTo("evci.eworker." + QUEUE_TEST + ".dlq");
    }

}
