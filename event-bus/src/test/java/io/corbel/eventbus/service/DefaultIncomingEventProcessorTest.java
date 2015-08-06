package io.corbel.eventbus.service;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.corbel.eventbus.Event;
import io.corbel.eventbus.EventHandler;

/**
 * @author Alexander De Leon
 *
 */
public class DefaultIncomingEventProcessorTest {

    Map<Class<? extends Event>, List<EventHandler<? extends Event>>> eventHandlerMap;
    EventBusRegistry registry;
    DefaultIncomingEventProcessor processor;

    EventHandler eventHandler1;
    EventHandler eventHandler2;
    EventHandler eventHandler3;

    class MyTestEvent1 implements Event {}

    class MyTestEvent2 implements Event {}

    @Before
    public void setup() {

        eventHandlerMap = spy(new HashMap<Class<? extends Event>, List<EventHandler<? extends Event>>>());

        registry = mock(EventBusRegistry.class);

        eventHandler1 = mock(EventHandler.class);
        eventHandler2 = mock(EventHandler.class);
        eventHandler3 = mock(EventHandler.class);

        when(eventHandler1.getEventType()).thenReturn(MyTestEvent1.class);
        when(eventHandler2.getEventType()).thenReturn(MyTestEvent1.class);
        when(eventHandler3.getEventType()).thenReturn(MyTestEvent2.class);

        when(registry.getHandlers(MyTestEvent1.class)).thenReturn(
                Arrays.<EventHandler<? extends Event>>asList(eventHandler1, eventHandler2));

        when(registry.getHandlers(MyTestEvent2.class)).thenReturn(Arrays.<EventHandler<? extends Event>>asList(eventHandler3));

        processor = new DefaultIncomingEventProcessor(registry);
    }

    @Test
    public void testInvoke() {

        MyTestEvent1 myTestEvent1 = new MyTestEvent1();
        MyTestEvent2 myTestEvent2 = new MyTestEvent2();

        processor.process(myTestEvent1);

        verify(eventHandler1, times(1)).handle(myTestEvent1);
        verify(eventHandler2, times(1)).handle(myTestEvent1);
        verify(eventHandler3, never()).handle(myTestEvent1);

        processor.process(myTestEvent2);

        verify(eventHandler1, never()).handle(myTestEvent2);
        verify(eventHandler2, never()).handle(myTestEvent2);
        verify(eventHandler3, times(1)).handle(myTestEvent2);

    }

}
