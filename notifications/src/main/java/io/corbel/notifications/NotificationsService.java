package io.corbel.notifications;

import io.corbel.lib.ws.cli.ServiceRunnerWithVersionResource;
import io.corbel.lib.ws.health.AuthorizationRedisHealthCheck;
import io.corbel.lib.ws.health.BasicHealthCheck;
import io.corbel.lib.ws.health.MongoHealthCheck;
import io.corbel.lib.ws.health.RabbitMQHealthCheck;
import io.corbel.notifications.api.DomainResource;
import io.corbel.notifications.api.NotificationsResource;
import io.corbel.notifications.ioc.NotificationsListenerIoc;
import io.dropwizard.setup.Environment;
import org.springframework.context.ApplicationContext;

public class NotificationsService extends ServiceRunnerWithVersionResource<NotificationsListenerIoc> {

	private final ApplicationContext springContext;

	public NotificationsService(ApplicationContext springContext) {
		this.springContext = springContext;
	}

	@Override
	protected String getArtifactId() {
		// This has to be the same as in pom.xml
		return "notifications";
	}

	@Override
	protected ApplicationContext loadSpringContext() {
		return springContext;
	}

	@Override
	protected void configureService(Environment environment, ApplicationContext context) {
		super.configureService(environment, context);
		environment.jersey().register(context.getBean(NotificationsResource.class));
		environment.jersey().register(context.getBean(DomainResource.class));
		environment.healthChecks().register("basic", new BasicHealthCheck());
		environment.healthChecks().register("redis",context.getBean(AuthorizationRedisHealthCheck.class));
		environment.healthChecks().register("mongo", context.getBean(MongoHealthCheck.class));
		environment.healthChecks().register("rabit",context.getBean(RabbitMQHealthCheck.class));
	}
}
