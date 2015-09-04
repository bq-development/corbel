package io.corbel.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.corbel.lib.cli.console.Console;
import io.corbel.lib.ws.log.LogbackUtils;
import io.corbel.oauth.cli.dsl.OauthShell;
import io.corbel.oauth.ioc.OauthIoc;

/**
 * @author Alexander De Leon
 */
public class OauthConsoleRunner extends Console {

    private static final Logger LOG = LoggerFactory.getLogger(OauthConsoleRunner.class);

    public OauthConsoleRunner() {
        super("Welcome to Oauth Server. Type oauth.help() to start.", "oauth", createShell());
    }

    @SuppressWarnings("resource")
    private static OauthShell createShell() {
        System.setProperty("conf.namespace", "oauth-server");
        return new AnnotationConfigApplicationContext(OauthIoc.class).getBean(OauthShell.class);
    }

    public static void main(String[] args) {
        LogbackUtils.setLogLevel("INFO");
        OauthConsoleRunner console = new OauthConsoleRunner();
        try {
            if (args.length == 0) {
                console.launch();
            } else {
                console.runScripts(args);
            }
            System.exit(0);
        } catch (Throwable e) {
            LOG.error(e.getMessage());
            System.exit(1);
        }
    }
}
