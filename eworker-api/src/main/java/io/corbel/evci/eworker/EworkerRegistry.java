package io.corbel.evci.eworker;

import java.util.regex.Pattern;

/**
 * @author Alexander De Leon
 * 
 */
public interface EworkerRegistry {

    Integer DEFAULT_THREADS_NUMBER = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * Registry a Eworker with evci
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param handleFailures When this is true the EWorker is connected to the dead-letter-queue to handle failed messages.
     * @param threadsNumber define the number of threads that will be used for operating with the queue
     */
    <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, boolean handleFailures, int threadsNumber);

    /**
     * Registry a Eworker with evci
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param handleFailures When this is true the EWorker is connected to the dead-letter-queue to handle failed messages.
     */
    default <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, boolean handleFailures) {
        registerEworker(eworker, routingPattern, queue, handleFailures, DEFAULT_THREADS_NUMBER);
    }

    /**
     * Registry a Eworker with evci (without error handling)
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     * @param threadsNumber define the number of threads that will be used for operating with the queue
     */
    default <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue, int threadsNumber) {
        registerEworker(eworker, routingPattern, queue, false, threadsNumber);
    }

    /**
     * Registry a Eworker with evci (without error handling)
     * 
     * @param eworker The Eworker implementation to register.
     * @param routingPattern The routing pattern for the queue that this Eworker is responsible. The pattern must follow the syntax from
     *        {@link Pattern}
     * @param queue The name of the queue that this Eworker is responsible.
     */
    default <E> void registerEworker(Eworker<E> eworker, String routingPattern, String queue) {
        registerEworker(eworker, routingPattern, queue, false, DEFAULT_THREADS_NUMBER);
    }

}
