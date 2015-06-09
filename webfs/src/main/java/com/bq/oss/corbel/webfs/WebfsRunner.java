package com.bq.oss.corbel.webfs;

import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bq.oss.lib.ws.cli.ServiceRunnerWithVersionResource;
import com.bq.oss.lib.ws.health.BasicHealthCheck;
import com.bq.oss.corbel.webfs.api.WebResource;
import com.bq.oss.corbel.webfs.ioc.WebfsIoc;

/**
 * @author Rubén Carrasco
 *
 */
public class WebfsRunner extends ServiceRunnerWithVersionResource<WebfsIoc> {

	private static final Logger LOG = LoggerFactory.getLogger(WebfsRunner.class);

	public static void main(String[] args) {
		try {
			new WebfsRunner().run(args);
		} catch (Exception e) {
			LOG.error("Unable to start webfs", e);
		}
	}

	@Override
	protected String getArtifactId() {
		// This has to be the same as in pom.xml
		return "webfs";
	}

	@Override
	protected void configureService(Environment environment, ApplicationContext context) {
		super.configureService(environment, context);
		environment.jersey().register(context.getBean(WebResource.class));
		environment.healthChecks().register("basic", new BasicHealthCheck());
	}

}
