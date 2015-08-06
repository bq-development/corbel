package io.corbel.evci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.corbel.evci.ioc.EvciIoc;

/**
 * Created by Alberto J. Rubio
 */
public class EvciRunner {

	private static final Logger LOG = LoggerFactory.getLogger(EvciRunner.class);

	public static void main(String[] args) {
		try {
			System.setProperty("conf.namespace", "evci");
			ApplicationContext context = new AnnotationConfigApplicationContext(EvciIoc.class);
			boolean restEnabled = context.getEnvironment().getProperty("evci.rest.enabled", Boolean.class);
			if (restEnabled) {
				LOG.info("Starting EVCI REST api.");
				new EvciService(context).run(args);
			}
		} catch (Exception e) {
			LOG.error("Unable to start evci", e);
		}
	}
}
