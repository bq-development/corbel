package io.corbel.notifications.handler;

import io.corbel.event.NotificationEvent;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.repository.DomainRepository;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.service.DefaultSenderNotificationsService;
import io.corbel.notifications.service.NotificationsDispatcher;
import io.corbel.notifications.service.SenderNotificationsService;
import io.corbel.notifications.template.NotificationFiller;
import io.corbel.notifications.utils.DomainNameIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Cristian del Cerro
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultSenderNotificationsServiceTest {

	@Mock
	private NotificationFiller notificationFiller;

	@Mock
	private NotificationsDispatcher notificationsDispatcher;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private DomainRepository domainRepository;

	private Map<String, String> properties = new HashMap<>();

	private SenderNotificationsService senderNotificationsService;

	@Before
	public void setUp() throws Exception {
		senderNotificationsService = new DefaultSenderNotificationsService(notificationFiller, notificationsDispatcher,
				notificationRepository, domainRepository);
	}

	@Test
	public void testTreatEvent() {
		String domain = "domain";
		String id = "id";
		String templateId = DomainNameIdGenerator.generateNotificationTemplateId(domain, id);

		NotificationEvent notificationEvent = new NotificationEvent(id, "recipient");
		notificationEvent.setDomain(domain);
		notificationEvent.setProperties(properties);
		NotificationTemplate notificationTemplate = new NotificationTemplate();
		when(notificationRepository.findOne(templateId)).thenReturn(notificationTemplate);
		when(notificationFiller.fill(notificationTemplate, properties)).thenReturn(notificationTemplate);

		senderNotificationsService.sendNotification(domain, notificationEvent.getNotificationId(),
				notificationEvent.getProperties(), notificationEvent.getRecipient());

		verify(notificationFiller, times(1)).fill(notificationTemplate, properties);
		verify(notificationsDispatcher, times(1)).send(notificationTemplate, "recipient");
	}

}
