package io.corbel.iam.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.event.*;
import io.corbel.eventbus.service.EventBus;
import io.corbel.iam.model.Device;
import io.corbel.iam.model.User;

/**
 * @author Rubén Carrasco
 *
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultEventsServiceTest {

    private static final String SCOPE = "scope";
    private static final String DOMAIN = "domain";
    private static final String ID = "id";
    private static final String EMAIL = "email";
    private static final String USERNAME = "username";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String PROFILEURL = "profileurl";
    private static final String PHONENUMBER = "phonenumber";
    private static final String COUNTRY = "country";
    private static final Map<String, Object> PROPERTIES = new HashMap<>();
    private static final Set<String> SCOPES = new HashSet<>();
    private static final Set<String> GROUPS = new HashSet<>();

    private static final String USERID = "userid";
    private static final String UID = "uid";
    private static final String NOTIFICATIONURI = "notificationuri";
    private static final String NAME = "name";
    private static final String TYPE = "Android";
    private static final Boolean NOTIFICATIONENABLED = true;

    private @Mock EventBus eventBus;
    private @Mock User user;
    private @Mock Device device;

    EventsService service;

    @Before
    public void setUp() throws Exception {
        when(user.getDomain()).thenReturn(DOMAIN);
        when(user.getId()).thenReturn(ID);
        when(user.getEmail()).thenReturn(EMAIL);
        when(user.getUsername()).thenReturn(USERNAME);
        when(user.getFirstName()).thenReturn(FIRSTNAME);
        when(user.getLastName()).thenReturn(LASTNAME);
        when(user.getProfileUrl()).thenReturn(PROFILEURL);
        when(user.getPhoneNumber()).thenReturn(PHONENUMBER);
        when(user.getCountry()).thenReturn(COUNTRY);
        when(user.getProperties()).thenReturn(PROPERTIES);
        when(user.getScopes()).thenReturn(SCOPES);
        when(user.getGroups()).thenReturn(GROUPS);

        when(device.getId()).thenReturn(ID);
        when(device.getDomain()).thenReturn(DOMAIN);
        when(device.getUserId()).thenReturn(USERID);
        when(device.getUid()).thenReturn(UID);
        when(device.getNotificationUri()).thenReturn(NOTIFICATIONURI);
        when(device.getName()).thenReturn(NAME);
        when(device.getType()).thenReturn(TYPE);
        when(device.isNotificationEnabled()).thenReturn(NOTIFICATIONENABLED);

        service = new DefaultEventsService(eventBus);
    }

    @Test
    public void testSendUserCreatedEvent() {
        service.sendUserCreatedEvent(user);
        verify(eventBus).dispatch(
                new UserCreatedEvent(DOMAIN, ID, EMAIL, USERNAME, FIRSTNAME, LASTNAME, PROFILEURL, PHONENUMBER, COUNTRY, PROPERTIES,
                        SCOPES, GROUPS));
    }

    @Test
    public void testSendUserDeletedEvent() {
        User user = new User();
        service.sendUserDeletedEvent(user, DOMAIN);
        verify(eventBus).dispatch(new UserDeletedEvent(null,null,null,null, DOMAIN));
    }

    @Test
    public void testSendUserModifiedEvent() {
        service.sendUserModifiedEvent(user);
        verify(eventBus).dispatch(
                new UserModifiedEvent(DOMAIN, ID, EMAIL, USERNAME, FIRSTNAME, LASTNAME, PROFILEURL, PHONENUMBER, COUNTRY, PROPERTIES,
                        SCOPES, GROUPS));
    }

    @Test
    public void testSendUserAuthenticationEvent() {
        service.sendUserAuthenticationEvent(user);
        verify(eventBus).dispatch(
                new UserAuthenticationEvent(DOMAIN, ID, EMAIL, USERNAME, FIRSTNAME, LASTNAME, PROFILEURL, PHONENUMBER, COUNTRY, PROPERTIES,
                        SCOPES, GROUPS));
    }

    @Test
    public void testSendNotificationEvent() {
        HashMap<String, String> properties = new HashMap<>();
        service.sendNotificationEvent("domain", "notificationId", "recipient", properties);

        NotificationEvent notificationEvent = new NotificationEvent("notificationId", "recipient", "domain");
        notificationEvent.setProperties(properties);
        verify(eventBus).dispatch(notificationEvent);
    }

    @Test
    public void testSendDomainDeletedEvent() {
        service.sendDomainDeletedEvent(DOMAIN);
        verify(eventBus).dispatch(new DomainDeletedEvent(DOMAIN));
    }

    @Test
    public void testSendCreateScope() {
        service.sendCreateScope(SCOPE);
        verify(eventBus).dispatch(ScopeUpdateEvent.createScopeEvent(SCOPE, null));
    }

    @Test
    public void testSendDeleteScope() {
        service.sendDeleteScope(SCOPE);
        verify(eventBus).dispatch(ScopeUpdateEvent.deleteScopeEvent(SCOPE, null));
    }

    @Test
    public void testSendClientAuthenticationEvent() {
        service.sendClientAuthenticationEvent(DOMAIN, ID);
        verify(eventBus).dispatch(AuthorizationEvent.clientAuthenticationEvent(DOMAIN, ID));
    }

    @Test
    public void testSendDeviceCreateEvent() {
        service.sendDeviceCreateEvent(device);
        verify(eventBus).dispatch(new DeviceEvent(DeviceEvent.Type.CREATED, DOMAIN, ID, USERID, TYPE, NAME));
    }

    @Test
    public void testSendDeviceUpdateEvent() {
        service.sendDeviceUpdateEvent(device);
        verify(eventBus).dispatch(new DeviceEvent(DeviceEvent.Type.UPDATED, DOMAIN, ID, USERID, TYPE, NAME));
    }

    @Test
    public void testSendDeviceDeleteEvent() {
        service.sendDeviceDeleteEvent(UID, USERID, DOMAIN);
        verify(eventBus).dispatch(new DeviceEvent(DeviceEvent.Type.DELETED, DOMAIN, ID, USERID));
    }

}
