package io.corbel.evci.ioc;

import io.corbel.evci.api.EventResource;
import io.corbel.evci.api.EworkerInfoResource;
import io.corbel.evci.client.DefaultEvciClient;
import io.corbel.evci.client.EvciClient;
import io.corbel.evci.converter.DomainObjectJsonMessageConverterFactory;
import io.corbel.evci.eventbus.EvciEventHandler;
import io.corbel.evci.eworker.EworkerArtifactIdRegistry;
import io.corbel.evci.eworker.EworkerRegistry;
import io.corbel.evci.service.DefaultEventService;
import io.corbel.evci.service.EvciMQ;
import io.corbel.evci.service.EventsService;
import io.corbel.event.EvciEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.eventbus.ioc.EventBusListeningIoc;
import io.corbel.eworker.internal.AmqpEworkerRegistry;
import io.corbel.eworker.internal.InMemoryArtifactIdRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.rabbitmq.config.AmqpConfiguration;
import io.corbel.lib.rabbitmq.config.AmqpConfigurer;
import io.corbel.lib.rabbitmq.config.BackoffOptions;
import io.corbel.lib.rabbitmq.ioc.AbstractRabbitMQConfiguration;
import io.corbel.lib.token.ioc.TokenIoc;
import io.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import io.corbel.lib.ws.cors.ioc.CorsIoc;
import io.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import io.corbel.lib.ws.dw.ioc.DropwizardIoc;
import io.corbel.lib.ws.dw.ioc.RabbitMQHealthCheckIoc;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Configuration 
@Import({ConfigurationIoC.class, DropwizardIoc.class, TokenIoc.class, AuthorizationIoc.class, CorsIoc.class,
        EventBusListeningIoc.class, RabbitMQHealthCheckIoc.class, CommonFiltersIoc.class}) 
@ComponentScan({"io.corbel.evci.eworker.plugin", "com.bqreaders.silkroad.evci.eworker.plugin"})
public class EvciIoc extends AbstractRabbitMQConfiguration {

    @Autowired private Environment env;

    @Override
    protected MessageConverter getMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public EventHandler<EvciEvent> getEvciEventHandler() {
        return new EvciEventHandler(eventService(), getObjectMapper());
    }

    @Bean
    public EventResource eventResource() {
        return new EventResource(eventService(), getObjectMapper());
    }

    @Bean(name = "evciAmqpTemplate")
    @Override
    public AmqpTemplate amqpTemplate() {
        return super.amqpTemplate();
    }

    @Bean
    public EventsService eventService() {
        return new DefaultEventService(amqpTemplate(), routingPatternFunction());
    }

    @Bean
    public AmqpConfiguration rabbitMQConfiguration() {
        return configurer -> {
            configurer.topicExchange(EvciMQ.EVENTS_EXCHANGE,
                    configurer.alternateExchange(configurer.fanoutExchange(EvciMQ.UNKNOWN_EXCHANGE)));
            configurer.bind(EvciMQ.UNKNOWN_EXCHANGE, configurer.queue(EvciMQ.UNKNOWN_QUEUE), Optional.<String>empty(),
                    Optional.<Map<String, Object>>empty());

            configurer.topicExchange(EvciMQ.EVENTS_DEAD_LETTER_EXCHANGE);
        };
    }

    @Bean
    public DomainObjectJsonMessageConverterFactory domainObjectJsonMessageConverterFactory(ObjectMapper objectMapper) {
        return new DomainObjectJsonMessageConverterFactory(objectMapper);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

    @Bean
    @DependsOn("rabbitMQConfiguration")
    public EworkerRegistry eworkerRegistry(AmqpConfigurer configurer, DomainObjectJsonMessageConverterFactory converterFactory) {
        return new AmqpEworkerRegistry(converterFactory, configurer, getBackoffOptions(), env.getProperty("rabbitmq.maxAttempts",
                Integer.class), routingPatternFunction());
    }

    @Bean
    public EvciClient evciClient() {
        return new DefaultEvciClient(eventService(), getObjectMapper());
    }

    private BackoffOptions getBackoffOptions() {
        BackoffOptions backoffOptions = new BackoffOptions();
        backoffOptions.setInitialInterval(env.getProperty("rabbitmq.backoff.initialInterval", Long.class));
        backoffOptions.setMultiplier(env.getProperty("rabbitmq.backoff.multiplier", Double.class));
        backoffOptions.setMaxInterval(env.getProperty("rabbitmq.backoff.maxInterval", Long.class));
        return backoffOptions;
    }

    @Bean
    public EworkerArtifactIdRegistry eworkerArtifactIdRegistry() {
        return new InMemoryArtifactIdRegistry();
    }

    @Bean
    public EworkerInfoResource eworkerInfoResource(EworkerArtifactIdRegistry eworkerArtifactIdRegistry) {
        return new EworkerInfoResource(eworkerArtifactIdRegistry);
    }

    private UnaryOperator<String> routingPatternFunction() {
        return type -> type.replace(":", ".");
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }
}
