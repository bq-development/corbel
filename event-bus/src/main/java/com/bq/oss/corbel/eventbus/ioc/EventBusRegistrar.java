package com.bq.oss.corbel.eventbus.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bq.oss.corbel.eventbus.EventHandler;
import com.bq.oss.corbel.eventbus.service.EventBusRegistry;

/**
 * @author Alexander De Leon
 *
 */
public class EventBusRegistrar implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusRegistrar.class);

    private final EventBusRegistry registry;

    public EventBusRegistrar(EventBusRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        for (EventHandler<?> handler : context.getBeansOfType(EventHandler.class).values()) {
            LOG.info("Registering event handler {} for events of type {}", handler.getClass().getName(), handler.getEventType().getName());
            registry.register(handler);
        }
    }
}
