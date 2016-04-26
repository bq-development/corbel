package io.corbel.notifications.api;


import com.google.gson.JsonElement;
import io.corbel.lib.queries.request.*;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.notifications.model.Domain;
import io.corbel.notifications.repository.DomainRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(ApiVersion.CURRENT + "/{domain}/domain")
public class DomainResource {

    private static final String DOMAIN = "domain";

    private final DomainRepository domainRepository;


    public DomainResource(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postDomain(@Valid Domain domain, @PathParam(DOMAIN) String domainId,
                               @Context UriInfo uriInfo) {
        domain.setId(domainId);
        domainRepository.save(domain);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(domain.getId()).build()).build();
    }

    @GET
    public Response getDomain(@PathParam(DOMAIN) String domainId) {
        Domain domain = domainRepository.findOne(domainId);
        if (domain == null) {
            return NotificationsErrorResponseFactory.getInstance().notFound();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(domain).build();
    }

    @PUT
    public Response updateDomain(Domain domainData, @PathParam(DOMAIN) String domainId) {

        Domain domain = domainRepository.findOne(domainId);

        if((domainData.getId() != null && !domainId.equals(domainData.getId()))
                || domain == null) {
            return ErrorResponseFactory.getInstance().notFound();
        }

        domain.updateDomain(domainData);
        domainRepository.save(domain);
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @DELETE
    public Response deleteDomain(@PathParam(DOMAIN) String domainId) {
        domainRepository.delete(domainId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
