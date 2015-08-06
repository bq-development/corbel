package io.corbel.evci.service;

/**
 * @author Alexander De Leon
 * 
 */
public interface EvciMQ {

    String EVENTS_EXCHANGE = "evci.exchange";
    String EVENTS_DEAD_LETTER_EXCHANGE = "evci.deadletter.exchange";
    String UNKNOWN_EXCHANGE = "evci.unknown.exchange";

    String UNKNOWN_QUEUE = "evci.unknown.queue";

}
