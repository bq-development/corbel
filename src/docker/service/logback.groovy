import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import ch.qos.logback.classic.Level

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level [%date{ISO8601}] %X{requestId} %logger{35} - %msg%n"
    }
}

//Get the log level from environment variable LOG_LEVEL
def env = System.getenv()
def level = Level.valueOf(Optional.ofNullable(env['LOG_LEVEL']).orElse("INFO"))

root(level, ["STDOUT"])