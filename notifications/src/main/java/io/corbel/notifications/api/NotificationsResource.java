package io.corbel.notifications.api;

import java.util.List;

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
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.notifications.model.Notification;
import io.corbel.notifications.model.NotificationTemplate;
import io.corbel.notifications.model.NotificationTemplateResponse;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.service.SenderNotificationsService;
import io.corbel.notifications.utils.DomainNameIdGenerator;
import io.dropwizard.auth.Auth;

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
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationTemplates).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postTemplate(@Valid NotificationTemplate notificationTemplate, @Context UriInfo uriInfo) {
        notificationTemplate.setId(null);
        notificationRepository.save(notificationTemplate);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(notificationTemplate.getName()).build()).build();
    }


    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTemplate(NotificationTemplate notificationTemplateData, @PathParam("domain") String domain,
                                   @PathParam("name") String name) {
        NotificationTemplate notificationTemplate = notificationRepository.findByDomainAndName(domain, name);

        if (notificationTemplate != null) {
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
        NotificationTemplateResponse notificationTemplateResponse = new NotificationTemplateResponse(notificationTemplate);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(notificationTemplateResponse).build();
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
        String id = DomainNameIdGenerator.generateNotificationTemplateId(domainId, notification.getNotificationId());
        senderNotificationsService.sendNotification(domainId, id, notification.getProperties(),
                notification.getRecipient());
        return Response.ok().build();
    }


}
