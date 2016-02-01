package io.corbel.resources.rem.plugin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.restor.ioc.RestorIoc;
import io.corbel.resources.rem.restor.ioc.RestorIocBeanNames;
import io.corbel.lib.config.ConfigurationHelper;
import com.codahale.metrics.health.HealthCheck;

/**
 * @author Alberto J. Rubio
 */
@Component public class RestorRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RestorRemPlugin.class);
    private static final String ARTIFACT_ID = "restor";

    @Override
    protected void init() {
        LOG.info("Initializing Restor plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RestorIoc.class);
    }

    @Override
    protected void register(RemRegistry registry) {
        @SuppressWarnings("unchecked")
        List<MediaType> acceptedMediaTypes = (List<MediaType>) context.getBean(RestorIocBeanNames.ACCEPTED_MEDIATYPES, List.class);

        for (MediaType mediaType : acceptedMediaTypes) {
            registry.registerRem(context.getBean(RestorIocBeanNames.RESTOR_GET, Rem.class), ".*", mediaType, HttpMethod.GET);
            registry.registerRem(context.getBean(RestorIocBeanNames.RESTOR_PUT, Rem.class), ".*", mediaType, HttpMethod.PUT);
            registry.registerRem(context.getBean(RestorIocBeanNames.RESTOR_DELETE, Rem.class), ".*", mediaType, HttpMethod.DELETE);
        }
    }

    @Override
    protected void addHealthCheck(HealthCheckRegistry healthCheckRegistry) {
        healthCheckRegistry.addHealthCheck(RestorIocBeanNames.HEALTH_CHECK,
                context.getBean(RestorIocBeanNames.HEALTH_CHECK, HealthCheck.class));
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
