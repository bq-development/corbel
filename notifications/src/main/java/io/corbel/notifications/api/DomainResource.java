package io.corbel.notifications.api;


import com.google.gson.JsonElement;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.queries.request.*;
import io.corbel.lib.ws.annotation.Rest;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.notifications.model.Domain;
import io.corbel.notifications.repository.DomainRepository;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path(ApiVersion.CURRENT + "/{domain}/domain")
public class DomainResource {

    private static final int BAD_REQUEST_STATUS = 400;
    private static final String DOMAIN = "domain";

    private final DomainRepository domainRepository;
    private final AggregationResultsFactory<JsonElement> aggregationResultsFactory;


    public DomainResource(DomainRepository domainRepository,
                          AggregationResultsFactory aggregationResultsFactory) {
        this.domainRepository = domainRepository;
        this.aggregationResultsFactory = aggregationResultsFactory;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNotificationConfig(@Valid Domain domain, @PathParam(DOMAIN) String domainId,
                                           @Context UriInfo uriInfo) {
        domain.setId(domainId);
        domainRepository.save(domain);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(domain.getId()).build()).build();
    }

    @GET
    public Response getNotificationsConfig(@Rest QueryParameters queryParameters, @PathParam(DOMAIN) String domainId) {
        if (queryParameters.getAggregation().isPresent()) {
            return getNotificationConfigAggregation(domainId, queryParameters.getQuery()
                    .orElse(null), queryParameters.getAggregation().orElse(null));
        } else {
            List<Domain> domainList = domainRepository.find(addDomainToQuery(domainId, queryParameters.getQuery()
                    .orElse(null)), queryParameters.getPagination(), queryParameters.getSort().orElse(null));

            return Response.ok().type(MediaType.APPLICATION_JSON).entity(domainList).build();
        }

    }

    private Response getNotificationConfigAggregation(String domainId, ResourceQuery query, Aggregation aggregation) {
        if (!AggregationOperator.$COUNT.equals(aggregation.getOperator())) {
            return Response.status(BAD_REQUEST_STATUS).build();
        }

        long count = domainRepository.count(addDomainToQuery(domainId, query));
        JsonElement result = aggregationResultsFactory.countResult(count);
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(result).build();
    }

    private ResourceQuery addDomainToQuery(String domain, ResourceQuery resourceQuery) {
        ResourceQueryBuilder builder = new ResourceQueryBuilder(resourceQuery);
        builder.remove(DOMAIN).add(DOMAIN, domain);
        return builder.build();
    }

    @GET
    @Path("/{id}")
    public Response getNotificationConfig(@PathParam("id") String id, @PathParam(DOMAIN) String domainId) {
        Domain domain = domainRepository.findOne(id);
        if (domain == null || !domainId.equals(domain.getId())) {
            return NotificationsErrorResponseFactory.getInstance().notFound();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(domain).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateNotificationConfig(Domain domainData, @PathParam(DOMAIN) String domainId,
                                             @PathParam("id") String id) {

        Domain domain = domainRepository.findOne(id);

        if((domainData.getId() != null && !domainId.equals(domainData.getId()))
                || domain == null) {
            return ErrorResponseFactory.getInstance().notFound();
        }

        domain.updateDomain(domainData);
        domainRepository.save(domain);
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @DELETE
    @Path("/{id}")
    public Response deleteNotificationConfig(@PathParam("id") String id, @PathParam(DOMAIN) String domainId) {
        Domain domain = domainRepository.findOne(id);
        if(domain.getId().equals(domainId)) {
            domainRepository.delete(id);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
