package com.bq.oss.corbel.eventbus.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.bq.oss.corbel.eventbus.Event;
import com.bq.oss.corbel.eventbus.EventHandler;

/**
 * @author Francisco Sanchez
 */
public class DefaultEventBusRegistryTest {
    Map<Class<? extends Event>, List<EventHandler<? extends Event>>> eventHandlerMap;
    InMemoryEventBusRegistry defaultEventBus;

    EventHandler eventHandler1;
    EventHandler eventHandler2;
    EventHandler eventHandler3;

    class MyTestEvent1 implements Event {}

    class MyTestEvent2 implements Event {}

    @Before
    public void setup() {

        defaultEventBus = new InMemoryEventBusRegistry();

        eventHandler1 = mock(EventHandler.class);
        eventHandler2 = mock(EventHandler.class);
        eventHandler3 = mock(EventHandler.class);

        when(eventHandler1.getEventType()).thenReturn(MyTestEvent1.class);
        when(eventHandler2.getEventType()).thenReturn(MyTestEvent1.class);
        when(eventHandler3.getEventType()).thenReturn(MyTestEvent2.class);
    }

    @Test
    public void testRegister() {
        ArgumentCaptor<List> eventHandlerListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);

        MyTestEvent1 myTestEvent1 = new MyTestEvent1();

        defaultEventBus.register(eventHandler1);
        defaultEventBus.register(eventHandler2);
        defaultEventBus.register(eventHandler3);

        Iterator<EventHandler<? extends Event>> event1Handlers = defaultEventBus.getHandlers(MyTestEvent1.class).iterator();
        Iterator<EventHandler<? extends Event>> event2Handlers = defaultEventBus.getHandlers(MyTestEvent2.class).iterator();
        assertThat(event1Handlers.next()).isSameAs(eventHandler1);
        assertThat(event1Handlers.next()).isSameAs(eventHandler2);
        assertThat(event1Handlers.hasNext()).isFalse();
        assertThat(event2Handlers.next()).isSameAs(eventHandler3);
        assertThat(event2Handlers.hasNext()).isFalse();
    }
}