package io.corbel.evci.eworker;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

/**
 * @author Alexander De Leon
 * 
 */
public interface EworkerRegistry {

    public static final int DEFAULT_THREADS_NUMBER = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * Registry a Eworker with evci
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker
     * @param handleFailures When this is true the EWorker is connected to the dead-letter-queue to handle failed messages.
     * @param threadsNumber define the number of threads that will be used for operating with the queue
     */
    <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, Class<E> messageType, boolean handleFailures,
            int threadsNumber);

    /**
     * Registry a Eworker with evci
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker
     * @param handleFailures When this is true the EWorker is connected to the dead-letter-queue to handle failed messages.
     */
    default <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, Class<E> messageType, boolean handleFailures) {
        registerEworker(eworker, routingPattern, queue, messageType, handleFailures, DEFAULT_THREADS_NUMBER);
    }

    /**
     * Registry a Eworker with evci (without error handling)
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker
     * @param threadsNumber define the number of threads that will be used for operating with the queue
     */
    default <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, Class<E> messageType, int threadsNumber) {
        registerEworker(eworker, routingPattern, queue, messageType, false, threadsNumber);
    }

    /**
     * Registry a Eworker with evci (without error handling)
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker
     */
    default <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, Class<E> messageType) {
        registerEworker(eworker, routingPattern, queue, messageType, false, DEFAULT_THREADS_NUMBER);
    }

    /**
     * Registry a Eworker with evci
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker
     * @param handleFailures When this is true the EWorker is connected to the dead-letter-queue to handle failed messages.
     * @param threadsNumber define the number of threads that will be used for operating with the queue
     */
    void registerEworker(Eworker<?> eworker, String routingPattern, String queue, Type messageType, boolean handleFailures,
            int threadsNumber);

    /**
     * Registry a Eworker with evci
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker
     * @param handleFailures When this is true the EWorker is connected to the dead-letter-queue to handle failed messages.
     */
    default void registerEworker(Eworker<?> eworker, String routingPattern, String queue, Type messageType, boolean handleFailures) {
        registerEworker(eworker, routingPattern, queue, messageType, handleFailures, DEFAULT_THREADS_NUMBER);
    }

    /**
     * Registry a Eworker with evci (without error handling)
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker.
     * @param threadsNumber define the number of threads that will be used for operating with the queue
     */
    default void registerEworker(Eworker<?> eworker, String routingPattern, String queue, Type messageType, int threadsNumber) {
        registerEworker(eworker, routingPattern, queue, messageType, false, threadsNumber);
    }

    /**
     * Registry a Eworker with evci (without error handling)
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param messageType The java type of the messages expected by the eworker.
     */
    default void registerEworker(Eworker<?> eworker, String routingPattern, String queue, Type messageType) {
        registerEworker(eworker, routingPattern, queue, messageType, false, DEFAULT_THREADS_NUMBER);
    }
}
