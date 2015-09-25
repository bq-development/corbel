package io.corbel.oauth.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AuthFilterRegistrar implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(AuthFilterRegistrar.class);

    private final FilterRegistry registry;

    public AuthFilterRegistrar(FilterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        for (AuthFilter filter : context.getBeansOfType(AuthFilter.class).values()) {
            LOG.info("Registering auth filter {} of domain {}", filter.getClass().getName(), filter.getDomain());
            registry.registerFilter(filter);
        }
    }

}
