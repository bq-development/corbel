package api;

import io.corbel.lib.queries.parser.*;
import io.corbel.lib.ws.api.error.GenericExceptionMapper;
import io.corbel.lib.ws.api.error.JsonValidationExceptionMapper;
import io.corbel.lib.ws.gson.GsonMessageReaderWriterProvider;
import io.corbel.lib.ws.queries.QueryParametersProvider;
import io.corbel.notifications.api.NotificationsResource;
import io.corbel.notifications.model.Notification;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.model.NotificationTemplateApi;
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

    private static final String TEMPLATE_NAME = "templateName";
    private static final String TEMPLATE_SENDER = "templateSender";
    private static final String TEMPLATE_TEXT = "templateText";
    private static final String TEMPLATE_TITLE = "templateTitle";
    private static final String TEMPLATE_TYPE = "templateType";
    private static final String TEMPLATE_ID = DOMAIN + ":" + TEMPLATE_NAME;


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
        NotificationTemplateApi notificationTemplateApi = getNotificationTemplateApi();

        NotificationTemplate notificationTemplateWithoutId = getNotificationTemplate();
        notificationTemplateWithoutId.setId(null);


        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplateApi), Response.class);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeaderString("Location")).contains(TEMPLATE_NAME);
    }

    @Test
    public void testAddNotificationTemplateWithoutName() {
        NotificationTemplateApi notificationTemplateApi = getNotificationTemplateApi();
        notificationTemplateApi.setId(null);


        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplateApi), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testAddNotificationTemplateWithoutSender() {
        NotificationTemplateApi notificationTemplateApi = getNotificationTemplateApi();
        notificationTemplateApi.setSender(null);


        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplateApi), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testAddNotificationTemplateWithoutText() {
        NotificationTemplateApi notificationTemplateApi = getNotificationTemplateApi();
        notificationTemplateApi.setText(null);


        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplateApi), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testAddNotificationTemplateWithoutType() {
        NotificationTemplateApi notificationTemplateApi = getNotificationTemplateApi();
        notificationTemplateApi.setType(null);


        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .post(Entity.json(notificationTemplateApi), Response.class);

        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testGetNotificationTemplate() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();

        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, TEMPLATE_NAME)).thenReturn(notificationTemplate);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(200);
        NotificationTemplateApi notificationTemplateApi = new NotificationTemplateApi(notificationTemplate);
        assertThat(response.readEntity(NotificationTemplateApi.class)).isEqualsToByComparingFields(notificationTemplateApi);
    }

    @Test
    public void testGetNotExistingNotificationTemplate() {
        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, TEMPLATE_NAME)).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testUpdateTypeNotificationTemplate() {
        NotificationTemplateApi notificationTemplateApiData = new NotificationTemplateApi();
        notificationTemplateApiData.setType("TYPE_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, TEMPLATE_NAME)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateApiData), Response.class);

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
        NotificationTemplateApi notificationTemplateApiData = new NotificationTemplateApi();
        notificationTemplateApiData.setSender("SENDER_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, TEMPLATE_NAME)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateApiData), Response.class);

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
        NotificationTemplateApi notificationTemplateApiData = new NotificationTemplateApi();
        notificationTemplateApiData.setText("TEXT_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, TEMPLATE_NAME)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateApiData), Response.class);

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
        NotificationTemplateApi notificationTemplateApiData = new NotificationTemplateApi();
        notificationTemplateApiData.setTitle("TITLE_UPDATED");

        NotificationTemplate notificationTemplateGotten = getNotificationTemplate();

        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, TEMPLATE_NAME)).thenReturn(notificationTemplateGotten);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateApiData), Response.class);

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
        NotificationTemplateApi notificationTemplateApiData = new NotificationTemplateApi();

        when(notificationRepositoryMock.findByDomainAndName(DOMAIN, "notExisting")).thenReturn(null);

        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/notExisting").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .put(Entity.json(notificationTemplateApiData), Response.class);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteNotificationTemplate() {
        Response response = RULE.client().target("/v1.0/" + DOMAIN + "/notification/" + TEMPLATE_NAME).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .delete(Response.class);

        verify(notificationRepositoryMock).deleteByDomainAndName(DOMAIN, TEMPLATE_NAME);
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

        verify(senderNotificationsServiceMock).sendNotification(DOMAIN, "notificationId", Collections.emptyMap(), "emailRecipient");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private NotificationTemplate getNotificationTemplate() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId(TEMPLATE_ID);
        notificationTemplate.setDomain(DOMAIN);
        notificationTemplate.setName(TEMPLATE_NAME);
        notificationTemplate.setSender(TEMPLATE_SENDER);
        notificationTemplate.setText(TEMPLATE_TEXT);
        notificationTemplate.setTitle(TEMPLATE_TITLE);
        notificationTemplate.setType(TEMPLATE_TYPE);

        return notificationTemplate;
    }

    private NotificationTemplateApi getNotificationTemplateApi() {
        NotificationTemplateApi notificationTemplateApi = new NotificationTemplateApi();
        notificationTemplateApi.setId(TEMPLATE_NAME);
        notificationTemplateApi.setSender(TEMPLATE_SENDER);
        notificationTemplateApi.setText(TEMPLATE_TEXT);
        notificationTemplateApi.setTitle(TEMPLATE_TITLE);
        notificationTemplateApi.setType(TEMPLATE_TYPE);

        return notificationTemplateApi;
    }



}
