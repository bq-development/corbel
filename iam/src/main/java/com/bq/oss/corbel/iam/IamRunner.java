package com.bq.oss.corbel.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bq.oss.corbel.iam.api.*;
import com.bq.oss.corbel.iam.ioc.IamIoc;
import com.bq.oss.lib.ws.cli.GenericConsole;
import com.bq.oss.lib.ws.cli.ServiceRunnerWithVersionResource;
import com.bq.oss.lib.ws.health.AuthorizationRedisHealthCheck;
import com.bq.oss.lib.ws.health.BasicHealthCheck;
import com.bq.oss.lib.ws.health.MongoHealthCheck;
import io.dropwizard.setup.Environment;

/**
 * @author Alexander De Leon
 * 
 */
public class IamRunner extends ServiceRunnerWithVersionResource<IamIoc> {

    private static final Logger LOG = LoggerFactory.getLogger(IamRunner.class);

    public static void main(String[] args) {
        try {
            IamRunner iamRunner = new IamRunner();
            iamRunner.setCommandLine(new GenericConsole(iamRunner.getArtifactId(), IamIoc.class));
            iamRunner.run(args);
        } catch (Exception e) {
            LOG.error("Unable to start iam", e);
        }
    }

    @Override
    protected String getArtifactId() {
        // This has to be the same as in pom.xml
        return "iam";
    }

    @Override
    protected void configureService(Environment environment, ApplicationContext context) {
        super.configureService(environment, context);
        environment.jersey().getResourceConfig().getContainerRequestFilters();
        environment.jersey().register(context.getBean(TokenResource.class));
        environment.jersey().register(context.getBean(UserResource.class));
        environment.jersey().register(context.getBean(UsernameResource.class));
        environment.jersey().register(context.getBean(DomainResource.class));
        environment.jersey().register(context.getBean(ScopeResource.class));
        environment.healthChecks().register("basic", new BasicHealthCheck());
        environment.healthChecks().register("redis", context.getBean(AuthorizationRedisHealthCheck.class));
        environment.healthChecks().register("mongo", context.getBean(MongoHealthCheck.class));
    }
}
