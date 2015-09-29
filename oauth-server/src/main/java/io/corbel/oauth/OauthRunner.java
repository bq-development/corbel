package io.corbel.oauth;

import io.corbel.oauth.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.corbel.lib.token.provider.SessionProvider;
import io.corbel.lib.ws.cli.GenericConsole;
import io.corbel.lib.ws.cli.ServiceRunnerWithVersionResource;
import io.corbel.lib.ws.health.BasicHealthCheck;
import io.corbel.lib.ws.health.MongoHealthCheck;
import io.corbel.oauth.ioc.OauthIoc;
import io.dropwizard.setup.Environment;

/**
 * @author Alexander De Leon
 *
 */
public class OauthRunner extends ServiceRunnerWithVersionResource<OauthIoc> {

    private static final Logger LOG = LoggerFactory.getLogger(OauthRunner.class);

    public static void main(String[] args) {
        try {
            OauthRunner oauthRunner = new OauthRunner();
            oauthRunner.setCommandLine(new GenericConsole(oauthRunner.getArtifactId(), OauthIoc.class));
            oauthRunner.run(args);
        } catch (Exception e) {
            LOG.error("Unable to start oauth-server", e);
        }
    }

    @Override
    protected String getArtifactId() {
        // This has to be the same as in pom.xml
        return "oauth-server";
    }

    @Override
    protected void configureService(Environment environment, ApplicationContext context) {
        super.configureService(environment, context);
        environment.jersey().register(context.getBean(SessionProvider.class).getBinder());
        environment.jersey().register(context.getBean(AuthorizeResource.class));
        environment.jersey().register(context.getBean(TokenResource.class));
        environment.jersey().register(context.getBean(UserResource.class));
        environment.jersey().register(context.getBean(UsernameResource.class));
        environment.jersey().register(context.getBean(SignoutResource.class));
        environment.healthChecks().register("basic", new BasicHealthCheck());
        environment.healthChecks().register("mongo", context.getBean(MongoHealthCheck.class));
    }
}
