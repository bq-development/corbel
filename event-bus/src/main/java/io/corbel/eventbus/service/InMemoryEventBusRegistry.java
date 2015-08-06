package io.corbel.eventbus.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.corbel.eventbus.Event;
import io.corbel.eventbus.EventHandler;

/**
 * @author Francisco Sanchez
 */
public class InMemoryEventBusRegistry implements EventBusRegistry {

    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> eventHandlerMap;

    public InMemoryEventBusRegistry() {
        this.eventHandlerMap = new ConcurrentHashMap<>();
    }

    public InMemoryEventBusRegistry(Map<Class<? extends Event>, List<EventHandler<? extends Event>>> eventHandlerMap) {
        this.eventHandlerMap = new ConcurrentHashMap<>(eventHandlerMap);
    }

    @Override
    public <E extends Event> void register(EventHandler<E> eventHandler) {
        Class<? extends Event> registerClass = eventHandler.getEventType();
        List<EventHandler<? extends Event>> handlerList = eventHandlerMap.get(registerClass);
        if (handlerList == null) {
            handlerList = new CopyOnWriteArrayList<>();
            eventHandlerMap.put(registerClass, handlerList);
        }
        handlerList.add(eventHandler);
    }

    @Override
    public Iterable<EventHandler<? extends Event>> getHandlers(Class<? extends Event> eventType) {
        List<EventHandler<? extends Event>> mergedList = new LinkedList<>();

        mergedList.addAll(eventHandlerMap.getOrDefault(eventType, Collections.<EventHandler<? extends Event>>emptyList()));
        mergedList.addAll(eventHandlerMap.getOrDefault(Event.class, Collections.<EventHandler<? extends Event>>emptyList()));

        return Collections.unmodifiableList(mergedList);
    }

}
