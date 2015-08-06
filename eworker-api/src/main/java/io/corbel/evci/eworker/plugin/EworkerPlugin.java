package io.corbel.evci.eworker.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.corbel.evci.client.EvciClient;
import io.corbel.evci.eworker.EworkerArtifactIdRegistry;
import io.corbel.evci.eworker.EworkerRegistry;
import io.corbel.evci.exception.InitializationEworkerException;
import com.github.zafarkhaja.semver.Version;

/**
 * Every Eworker plugin must provide a subclass of this class on the package com.bqreaders.silkroad.evci.eworker.plugin
 *
 * NOTE: All subclasses MUST be annotated with {@link Component} to be properly registered
 *
 * @author Alberto J. Rubio
 *
 */
public abstract class EworkerPlugin implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EworkerPlugin.class);

    @Autowired protected EvciClient evciClient;
    @Autowired private EworkerRegistry registry;

    @Autowired private EworkerArtifactIdRegistry eworkerArtifactIdRegistry;

    @Value("${platform.version}") protected String version;

    @Value("${evci.plugins.resilient}") private boolean resilient;

    protected Integer threadsNumber;

    protected ApplicationContext context;

    @Override
    public final void afterPropertiesSet() throws Exception {
        try {
            init();
            checkVersion();
            setThreadsNumber();
            register(registry);
            eworkerArtifactIdRegistry.addEworkerArtifactId(getArtifactName());
        } catch (Exception e) {
            if (resilient) {
                LOG.error("Error with " + this.getClass().getSimpleName(), e);
            } else {
                throw e;
            }
        }
    }

    private void setThreadsNumber() {
        threadsNumber = context.getEnvironment().getProperty("evci.plugins.concurrency", Integer.class,
                EworkerRegistry.DEFAULT_THREADS_NUMBER);
    }

    protected void checkVersion() {
        String version = context.getEnvironment().getProperty("platform.version");
        if (version != null) {
            if (Version.valueOf(version).getMajorVersion() != Version.valueOf(this.version).getMajorVersion()) {
                throw new InitializationEworkerException("Problem with eworker init: current platform version is " + this.version
                        + " but eworker uses " + version);
            }
        }
    }

    /**
     * Override this method to add initialization code.
     */
    protected void init() {}

    /**
     * Register all your Eworkers with the {@link EworkerRegistry}
     *
     * @param registry the registry
     *
     */
    protected abstract void register(EworkerRegistry registry);

    protected abstract String getArtifactName();

}
