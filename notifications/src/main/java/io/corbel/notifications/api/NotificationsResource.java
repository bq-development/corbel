package io.corbel.notifications.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.notifications.model.Notification;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.model.NotificationTemplateApi;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.service.SenderNotificationsService;
import io.corbel.notifications.utils.DomainNameIdGenerator;

/**
 * @author Francisco Sanchez
 */
@Path(ApiVersion.CURRENT + "/{domain}/notification")
public class NotificationsResource {

    private final NotificationRepository notificationRepository;
    private final SenderNotificationsService senderNotificationsService;

    public NotificationsResource(NotificationRepository notificationRepository,
                                 SenderNotificationsService senderNotificationsService) {
        this.notificationRepository = notificationRepository;
        this.senderNotificationsService = senderNotificationsService;
    }

    @GET
    public Response getTemplates(@Rest QueryParameters queryParameters) {
        List<NotificationTemplate> notificationTemplates = notificationRepository.find(queryParameters.getQuery()
                .orElse(null), queryParameters.getPagination(), queryParameters.getSort().orElse(null));

        List<NotificationTemplateApi> notificationTemplateApis = notificationTemplates.stream()
                .map(NotificationTemplateApi::new)
                .collect(Collectors.toList());

        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationTemplateApis).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postTemplate(@Valid NotificationTemplateApi notificationTemplateApi,  @PathParam("domain") String domain,
                                 @Context UriInfo uriInfo) {

        NotificationTemplate notificationTemplate = new NotificationTemplate(domain, notificationTemplateApi);
        notificationRepository.save(notificationTemplate);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(notificationTemplate.getName()).build()).build();
    }


    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTemplate(NotificationTemplateApi notificationTemplateApiData, @PathParam("domain") String domain,
                                   @PathParam("name") String name) {
        NotificationTemplate notificationTemplate = notificationRepository.findByDomainAndName(domain, name);

        if (notificationTemplate != null) {
            notificationTemplateApiData.setId(null);
            NotificationTemplate notificationTemplateData = new NotificationTemplate(domain, notificationTemplateApiData);
            notificationTemplate.updateTemplate(notificationTemplateData);
            notificationRepository.save(notificationTemplate);
            return Response.status(Status.NO_CONTENT).build();
        } else {
            return ErrorResponseFactory.getInstance().notFound();
        }
    }

    @GET
    @Path("/{name}")
    public Response getTemplate(@PathParam("domain") String domain, @PathParam("name") String name) {
        NotificationTemplate notificationTemplate = notificationRepository.findByDomainAndName(domain, name);
        if (notificationTemplate == null) {
            return NotificationsErrorResponseFactory.getInstance().notFound();
        }
        NotificationTemplateApi notificationTemplateApi = new NotificationTemplateApi(notificationTemplate);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationTemplateApi).build();
    }

    @DELETE
    @Path("/{name}")
    public Response deleteTemplate(@PathParam("domain") String domain, @PathParam("name") String name) {
        notificationRepository.deleteByDomainAndName(domain, name);
        return Response.status(Status.NO_CONTENT).build();
    }

    @POST
    @Path("/send")
    public Response postNotification(@Valid Notification notification, @PathParam("domain") String domainId) {
        senderNotificationsService.sendNotification(domainId, notification.getNotificationId(), notification.getProperties(),
                notification.getRecipient());
        return Response.ok().build();
    }


}
