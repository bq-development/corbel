package com.bq.oss.corbel.resources.rem.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.bq.oss.corbel.resources.rem.RemRegistry;
import com.bq.oss.corbel.resources.rem.exception.InitializationRemException;
import com.bq.oss.corbel.resources.rem.model.Mode;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.github.zafarkhaja.semver.Version;

/**
 * Every REM plugin must provide a subclass of this class on the package com.bq.oss.corbel.resources.rem.plugin
 *
 * NOTE: All subclasses MUST be annotated with {@link Component} to be properly registered
 *
 * @author Alexander De Leon
 *
 */
public abstract class RemPlugin implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(RemPlugin.class);

    @Autowired protected Mode mode;

    @Autowired protected RemService remService;

    @Autowired protected RemRegistry registry;

    @Autowired protected RelationRegistry relationRegistry;

    @Autowired protected HealthCheckRegistry healthCheckRegistry;

    @Autowired private PluginArtifactIdRegistry pluginArtifactIdRegistry;

    @Value("${platform.version}") protected String version;

    @Value("${resources.plugins.resilient}") private boolean resilient;

    protected ApplicationContext context;

    @Override
    public final void afterPropertiesSet() throws Exception {
        if (Mode.SERVICE.equals(mode)) {
            try {
                init();
                checkVersion();
                register(registry);
                addRelations(relationRegistry);
                addHealthCheck(healthCheckRegistry);
                pluginArtifactIdRegistry.addPluginArtifactId(getArtifactName());
            } catch (Exception e) {
                if (resilient) {
                    LOG.error("Error with " + this.getClass().getSimpleName(), e);
                } else {
                    throw e;
                }
            }
        } else {
            console();
        }
    }

    protected void checkVersion() {
        String version = context.getEnvironment().getProperty("platform.version");
        if (version != null) {
            if (Version.valueOf(version).getMajorVersion() != Version.valueOf(this.version).getMajorVersion()) {
                throw new InitializationRemException("Problem with rem init: current platform version is " + this.version
                        + " but rem uses " + version);
            }
        }
    }

    /**
     * Override this method to add initialization code.
     */
    protected void init() {}

    /**
     * Override this method to add initialization code.
     */
    protected void console() {}

    /**
     * Register all your REMs with the {@link RemRegistry}
     *
     * @param registry the registry
     */
    protected abstract void register(RemRegistry registry);

    /**
     * Register all your Relations with the {@link RelationRegistry}
     *
     * @param linkRegistry the registry
     */
    protected void addRelations(RelationRegistry linkRegistry) {}

    /**
     * Register all your HealthCheck with the {@link HealthCheckRegistry}
     *
     * @param healthCheckRegistry the registry
     */
    protected void addHealthCheck(HealthCheckRegistry healthCheckRegistry) {}

    protected abstract String getArtifactName();
}
