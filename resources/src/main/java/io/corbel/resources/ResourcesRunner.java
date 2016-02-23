package io.corbel.resources;

import io.corbel.lib.ws.cli.CommandLineI;
import io.corbel.lib.ws.cli.GenericConsole;
import io.corbel.lib.ws.cli.ServiceRunnerWithVersionResource;
import io.corbel.lib.ws.filter.ETagResponseFilter;
import io.corbel.lib.ws.health.AuthorizationRedisHealthCheck;
import io.corbel.lib.ws.health.BasicHealthCheck;
import io.corbel.lib.ws.health.MongoHealthCheck;
import io.corbel.resources.api.PluginInfoResource;
import io.corbel.resources.api.RemResource;
import io.corbel.resources.ioc.ResourcesIoc;
import io.corbel.resources.rem.plugin.HealthCheckRegistry;
import io.dropwizard.setup.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

public class ResourcesRunner extends ServiceRunnerWithVersionResource<ResourcesIoc> {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesRunner.class);

    public static void main(String[] args) {
        try {
            ResourcesRunner resourcesRunner = new ResourcesRunner();
            resourcesRunner.setCommandLine(createConsoleCommandLine(resourcesRunner));
            resourcesRunner.run(args);
        } catch (Exception e) {
            LOG.error("Unable to start resource", e);
        }
    }

    @Override
    protected String getArtifactId() {
        // This has to be the same as in pom.xml
        return "resources";
    }

    @Override
    protected void configureService(Environment environment, ApplicationContext context) {
        super.configureService(environment, context);
        environment.jersey().register(context.getBean(RemResource.class));
        environment.jersey().register(context.getBean(PluginInfoResource.class));
        environment.jersey().register(ETagResponseFilter.class);
        environment.healthChecks().register("basic", new BasicHealthCheck());
        environment.healthChecks().register("redis", context.getBean(AuthorizationRedisHealthCheck.class));
        environment.healthChecks().register("mongo", context.getBean(MongoHealthCheck.class));

        HealthCheckRegistry healthCheckRegistry = context.getBean(HealthCheckRegistry.class);
        healthCheckRegistry.getHealthChecks().forEach((k, v) -> environment.healthChecks().register(k, v));
    }

    @Override
    protected void configureObjectMapper(ObjectMapper objectMapperFactory) {
        objectMapperFactory.registerModule(new DefaultScalaModule());
        super.configureObjectMapper(objectMapperFactory);

    }

    private static CommandLineI createConsoleCommandLine(ResourcesRunner resourcesRunner) {
        return new GenericConsole(resourcesRunner.getArtifactId(), ResourcesIoc.class);
    }

}
