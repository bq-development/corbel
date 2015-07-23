package com.bq.oss.corbel.iam;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.bq.oss.corbel.iam.cli.dsl.IamShell;
import com.bq.oss.corbel.iam.ioc.IamIoc;
import io.corbel.lib.cli.console.Console;
import io.corbel.lib.ws.log.LogbackUtils;

/**
 * @author Alexander De Leon
 * 
 */
public class IamConsoleRunner extends Console {

    public IamConsoleRunner() {
        super("Welcome to Corbel IAM. Type iam.help() to start.", "iam", createShell());
    }

    @SuppressWarnings("resource")
    private static IamShell createShell() {
        System.setProperty("conf.namespace", "iam");
        return new AnnotationConfigApplicationContext(IamIoc.class).getBean(IamShell.class);
    }

    public static void main(String[] args) {
        LogbackUtils.setLogLevel("INFO");
        IamConsoleRunner console = new IamConsoleRunner();
        try {
            if (args.length == 0) {
                console.launch();
            } else {
                console.runScripts(args);
            }
            System.exit(0);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
