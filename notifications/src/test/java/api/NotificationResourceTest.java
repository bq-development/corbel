package api;

import io.corbel.lib.queries.parser.*;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.api.error.JsonValidationExceptionMapper;
import io.corbel.lib.ws.gson.GsonMessageReaderWriterProvider;
import io.corbel.lib.ws.queries.QueryParametersProvider;
import io.corbel.notifications.api.NotificationsResource;
import io.corbel.notifications.model.Notification;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.service.SenderNotificationsService;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;


public class NotificationResourceTest {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;
    private static final String TEST_TOKEN = "xxxx";
    private static final String AUTHORIZATION = "Authorization";
    private static final String DOMAIN = "domain";

    private static final String TEMPLATE_ID = "templateId";
    private static final String TEMPLATE_SENDER = "templateSender";
    private static final String TEMPLATE_TEXT = "templateText";
    private static final String TEMPLATE_TITLE = "templateTitle";
    private static final String TEMPLATE_TYPE = "templateType";

    private static NotificationRepository notificationRepositoryMock = mock(NotificationRepository.class);
    private static SenderNotificationsService senderNotificationsServiceMock = mock(SenderNotificationsService.class);
    private static QueryParser queryParserMock = mock(QueryParser.class);
    private static AggregationParser aggregationParserMock = mock(AggregationParser.class);
    private static PaginationParser paginationParserMock = mock(PaginationParser.class);
    private static SortParser sortParserMock = mock(SortParser.class);
    private static SearchParser searchParserMock = mock(SearchParser.class);


    @ClassRule
    public static ResourceTestRule RULE = ResourceTestRule
            .builder()
            .addProvider(new GsonMessageReaderWriterProvider())
            .addResource(new NotificationsResource(notificationRepositoryMock, senderNotificationsServiceMock))
            .addProvider(
                    new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, new QueryParametersParser(queryParserMock,
                            aggregationParserMock, sortParserMock, paginationParserMock, searchParserMock)).getBinder())
            .addProvider(GenericExceptionMapper.class).addProvider(JsonValidationExceptionMapper.class).build();

    @Before
    public void setUp() {
        reset(notificationRepositoryMock, senderNotificationsServiceMock);
    }

    @Test
    public void testAddNotificationTemplate() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();

        when(notificationRepositoryMock.save(Mockito.eq(notificationTemplate))).thenReturn(notificationTemplate);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplate), Response.class);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEMPLATE_ID);
    }

    @Test
    public void testAddNotificationTemplateWithoutSender() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();
        notificationTemplate.setSender(null);

        when(notificationRepositoryMock.save(Mockito.eq(notificationTemplate))).thenReturn(notificationTemplate);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplate), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testAddNotificationTemplateWithoutText() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();
        notificationTemplate.setText(null);

        when(notificationRepositoryMock.save(Mockito.eq(notificationTemplate))).thenReturn(notificationTemplate);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplate), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testAddNotificationTemplateWithoutType() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();
        notificationTemplate.setType(null);

        when(notificationRepositoryMock.save(Mockito.eq(notificationTemplate))).thenReturn(notificationTemplate);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplate), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testGetNotificationTemplate() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();

        when(notificationRepositoryMock.findOne(TEMPLATE_ID)).thenReturn(notificationTemplate);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(NotificationTemplate.class)).isEqualsToByComparingFields(notificationTemplate);
    }

    @Test
    public void testGetNotExistingNotificationTemplate() {
        when(notificationRepositoryMock.findOne(TEMPLATE_ID)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testUpdateTypeNotificationTemplate() {
        NotificationTemplate notificationTemplateData = new NotificationTemplate();
        notificationTemplateData.setType("TYPE_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findOne(TEMPLATE_ID)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateData), Response.class);

        ArgumentCaptor<NotificationTemplate> notificationTemplateCaptor = forClass(NotificationTemplate.class);
        verify(notificationRepositoryMock).save(notificationTemplateCaptor.capture());
        assertThat(notificationTemplateCaptor.getValue().getType()).isEqualTo("TYPE_UPDATED");
        assertThat(notificationTemplateCaptor.getValue().getSender()).isEqualTo(TEMPLATE_SENDER);
        assertThat(notificationTemplateCaptor.getValue().getText()).isEqualTo(TEMPLATE_TEXT);
        assertThat(notificationTemplateCaptor.getValue().getTitle()).isEqualTo(TEMPLATE_TITLE);
        assertThat(notificationTemplateCaptor.getValue().getId()).isEqualTo(TEMPLATE_ID);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testUpdateSenderNotificationTemplate() {
        NotificationTemplate notificationTemplateData = new NotificationTemplate();
        notificationTemplateData.setSender("SENDER_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findOne(TEMPLATE_ID)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateData), Response.class);

        ArgumentCaptor<NotificationTemplate> notificationTemplateCaptor = forClass(NotificationTemplate.class);
        verify(notificationRepositoryMock).save(notificationTemplateCaptor.capture());
        assertThat(notificationTemplateCaptor.getValue().getType()).isEqualTo(TEMPLATE_TYPE);
        assertThat(notificationTemplateCaptor.getValue().getSender()).isEqualTo("SENDER_UPDATED");
        assertThat(notificationTemplateCaptor.getValue().getText()).isEqualTo(TEMPLATE_TEXT);
        assertThat(notificationTemplateCaptor.getValue().getTitle()).isEqualTo(TEMPLATE_TITLE);
        assertThat(notificationTemplateCaptor.getValue().getId()).isEqualTo(TEMPLATE_ID);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testUpdateTextNotificationTemplate() {
        NotificationTemplate notificationTemplateData = new NotificationTemplate();
        notificationTemplateData.setText("TEXT_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findOne(TEMPLATE_ID)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateData), Response.class);

        ArgumentCaptor<NotificationTemplate> notificationTemplateCaptor = forClass(NotificationTemplate.class);
        verify(notificationRepositoryMock).save(notificationTemplateCaptor.capture());
        assertThat(notificationTemplateCaptor.getValue().getType()).isEqualTo(TEMPLATE_TYPE);
        assertThat(notificationTemplateCaptor.getValue().getSender()).isEqualTo(TEMPLATE_SENDER);
        assertThat(notificationTemplateCaptor.getValue().getText()).isEqualTo("TEXT_UPDATED");
        assertThat(notificationTemplateCaptor.getValue().getTitle()).isEqualTo(TEMPLATE_TITLE);
        assertThat(notificationTemplateCaptor.getValue().getId()).isEqualTo(TEMPLATE_ID);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testUpdateTitleNotificationTemplate() {
        NotificationTemplate notificationTemplateData = new NotificationTemplate();
        notificationTemplateData.setTitle("TITLE_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findOne(TEMPLATE_ID)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateData), Response.class);

        ArgumentCaptor<NotificationTemplate> notificationTemplateCaptor = forClass(NotificationTemplate.class);
        verify(notificationRepositoryMock).save(notificationTemplateCaptor.capture());
        assertThat(notificationTemplateCaptor.getValue().getType()).isEqualTo(TEMPLATE_TYPE);
        assertThat(notificationTemplateCaptor.getValue().getSender()).isEqualTo(TEMPLATE_SENDER);
        assertThat(notificationTemplateCaptor.getValue().getText()).isEqualTo(TEMPLATE_TEXT);
        assertThat(notificationTemplateCaptor.getValue().getTitle()).isEqualTo("TITLE_UPDATED");
        assertThat(notificationTemplateCaptor.getValue().getId()).isEqualTo(TEMPLATE_ID);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testUpdateNotExistingNotificationTemplate() {
        NotificationTemplate notificationTemplateData = new NotificationTemplate();

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/notExisting").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateData), Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteNotificationTemplate() {
        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_ID).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .delete(Response.class);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void testSendNotification() {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setProperties(Collections.emptyMap());
        notification.setRecipient("emailRecipient");
        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/send").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notification), Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private NotificationTemplate getNotificationTemplate() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId(TEMPLATE_ID);
        notificationTemplate.setSender(TEMPLATE_SENDER);
        notificationTemplate.setText(TEMPLATE_TEXT);
        notificationTemplate.setTitle(TEMPLATE_TITLE);
        notificationTemplate.setType(TEMPLATE_TYPE);

        return notificationTemplate;
    }



}
