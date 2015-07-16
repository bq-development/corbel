package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.Event;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.fest.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.Collections;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Francisco Sanchez
 */
public class EventSerializationTest {

    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    private Object marshallingAndUnmarsalling(Object object) {
        Message message = jackson2JsonMessageConverter.toMessage(object, null);
        return jackson2JsonMessageConverter.fromMessage(message);
    }

    private void assertThanCanBeSendAndRetriveInEventBus(Event event) {
        Event eventConv = (Event) marshallingAndUnmarsalling(event);
        assertThat(event).isEqualTo(eventConv);
    }

    @Before
    public void setup() {
        ObjectMapper mapper = new ObjectMapper();
        jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setJsonObjectMapper(mapper);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
    }

    @Test
    public void testAssetsEvent() {
        assertThanCanBeSendAndRetriveInEventBus(new AssetsEvent("DOMAIN", "USERID", Collections.singletonList(new AssetsEvent.EventAsset(Sets
                .newLinkedHashSet("a", "b"), new Date(), "ASSET_NAME", "PRODUCT_ID"))));
    }

    @Test
    public void testEvciEvent() {
        assertThanCanBeSendAndRetriveInEventBus(new EvciEvent("TEST_TYPE", "TEST_DATA"));
    }

    @Test
    public void testNotificationEvent() {
        assertThanCanBeSendAndRetriveInEventBus(new NotificationEvent("NOTIFICATION_ID", "RECIPIENT", "DOMAIN"));
    }

    @Test
    public void testPaymentEvent() {
        assertThanCanBeSendAndRetriveInEventBus(new PaymentEvent("USER_ID", "DOMAIN", "PRODUCT_ID", PaymentEvent.Type.NEW_PAYMENT_PLAN));
        assertThanCanBeSendAndRetriveInEventBus(new PaymentEvent("USER_ID", "DOMAIN", "PRODUCT_ID",
                PaymentEvent.Type.RECURRING_PAYMENT_FAILURE));
    }

    @Test
    public void testResourceCreatedEvent() {
        assertThanCanBeSendAndRetriveInEventBus(ResourceEvent.createResourceEvent("TYPE", "RESOURCE_ID", "DOMAIN_ID", "USER_ID"));
    }

    @Test
    public void testResourceModifiedEvent() {
        assertThanCanBeSendAndRetriveInEventBus(ResourceEvent.updateResourceEvent("TYPE", "RESOURCE_ID", "DOMAIN_ID", "USER_ID"));
    }

    @Test
    public void testResourceDeletedEvent() {
        assertThanCanBeSendAndRetriveInEventBus(ResourceEvent.deleteResourceEvent("TYPE", "RESOURCE_ID", "DOMAIN_ID", "USER_ID"));
    }

    @Test
    public void testUserCreatedEvent() {
        assertThanCanBeSendAndRetriveInEventBus(new UserCreatedEvent("USER", "DOMAIN", "EMAIL", "COUNTRY"));
    }

    @Test
    public void testUserDeletedEvent() {
        assertThanCanBeSendAndRetriveInEventBus(new UserDeletedEvent("USER_ID", "DOMAIN"));
    }
}
