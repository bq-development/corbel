package io.corbel.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.corbel.iam.api.*;
import io.corbel.iam.ioc.IamIoc;

import io.corbel.lib.ws.cli.GenericConsole;
import io.corbel.lib.ws.cli.ServiceRunnerWithVersionResource;
import io.corbel.lib.ws.health.AuthorizationRedisHealthCheck;
import io.corbel.lib.ws.health.BasicHealthCheck;
import io.corbel.lib.ws.health.MongoHealthCheck;
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
        environment.jersey().register(context.getBean(TokenResource.class));
        environment.jersey().register(context.getBean(EmailResource.class));
        environment.jersey().register(context.getBean(UserResource.class));
        environment.jersey().register(context.getBean(UsernameResource.class));
        environment.jersey().register(context.getBean(DomainResource.class));
        environment.jersey().register(context.getBean(ClientResource.class));
        environment.jersey().register(context.getBean(ScopeResource.class));
        environment.jersey().register(context.getBean(GroupResource.class));
        environment.healthChecks().register("basic", new BasicHealthCheck());
        environment.healthChecks().register("redis", context.getBean(AuthorizationRedisHealthCheck.class));
        environment.healthChecks().register("mongo", context.getBean(MongoHealthCheck.class));
    }

}
