package com.bq.oss.corbel.iam.api;

import io.dropwizard.auth.Auth;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.bq.oss.corbel.iam.exception.ClientAlreadyExistsException;
import com.bq.oss.corbel.iam.exception.DomainAlreadyExists;
import com.bq.oss.corbel.iam.exception.InvalidAggregationException;
import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.model.TraceableEntity;
import com.bq.oss.corbel.iam.service.ClientService;
import com.bq.oss.corbel.iam.service.DomainService;
import com.bq.oss.corbel.iam.utils.Message;
import com.bq.oss.lib.queries.jaxrs.QueryParameters;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;
import com.bq.oss.lib.ws.annotation.Rest;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.model.Error;
import com.google.common.base.Strings;

@Path(ApiVersion.CURRENT + "/domain") public class DomainResource {
    private final ClientService clientService;
    private final DomainService domainService;

    public DomainResource(ClientService clientService, DomainService domainService) {
        this.clientService = clientService;
        this.domainService = domainService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDomain(@Context UriInfo uriInfo, @Auth AuthorizationInfo authorizationInfo, Domain domain) {
        if (Strings.isNullOrEmpty(domain.getId())) {
            throw new ConstraintViolationException("Empty domain id", Collections.emptySet());
        }
        if (domain.getId().contains(Domain.ID_SEPARATOR)) {
            return IamErrorResponseFactory.getInstance().invalidEntity(
                    new Error("invalid_domain_id", Message.INVALID_DOMAIN_ID.getMessage()));
        }
        domain.setId(authorizationInfo.getDomainId() + Domain.ID_SEPARATOR + domain.getId());
        addTrace(domain);
        try {
            domainService.insert(domain);
        } catch (DomainAlreadyExists domainAlreadyExists) {
            return IamErrorResponseFactory.getInstance().entityExists(Message.DOMAIN_EXISTS, domainAlreadyExists.getDomain());
        }

        return Response.created(uriInfo.getAbsolutePathBuilder().path(domain.getId()).build()).build();
    }

    @GET
    @Path("/{domainId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomain(@PathParam("domainId") String domainId) {
        return domainService.getDomain(domainId).map(domain -> Response.ok(domain).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDomains(@Rest QueryParameters queryParameters) {
        ResourceQuery query = queryParameters.getQuery().orElse(null);

        return queryParameters.getAggregation().map(aggregation -> {
            try {
                AggregationResult result = domainService.getDomainsAggregation(query, aggregation);
                return Response.ok(result).build();

            } catch (InvalidAggregationException e) {
                return ErrorResponseFactory.getInstance().badRequest();
            }
        }).orElseGet(() -> {
            Pagination pagination = queryParameters.getPagination();
            Sort sort = queryParameters.getSort().orElse(null);
            return Response.ok(domainService.getAll(query, pagination, sort)).build();
        });
    }

    @PUT
    @Path("/{domainId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyDomain(@PathParam("domainId") String domainId, Domain domain) {
        domain.setId(domainId);
        addTrace(domain);
        domainService.update(domain);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{domainId}")
    public Response deleteDomain(@PathParam("domainId") String domain) {
        domainService.delete(domain);
        return Response.noContent().build();
    }

    @POST
    @Path("/{domainId}/client")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClient(@Context UriInfo uriInfo, @Valid Client client, @PathParam("domainId") String domainId) {

        Optional<Domain> domain = domainService.getDomain(domainId);

        if (!domain.isPresent()) {
            return IamErrorResponseFactory.getInstance().domainNotExists(domainId);
        }

        if (!domainService.scopesAllowedInDomain(client.getScopes(), domain.get())) {
            return IamErrorResponseFactory.getInstance().scopesNotAllowed(domainId);
        }

        client.setDomain(domainId);
        addTrace(client);

        try {
            clientService.createClient(client);
        } catch (ClientAlreadyExistsException e) {
            return IamErrorResponseFactory.getInstance().conflict(
                    new com.bq.oss.lib.ws.model.Error("conflict", "The client already exists"));
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(client.getId()).build()).build();
    }

    @GET
    @Path("/{domainId}/client/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClient(@PathParam("clientId") String clientId, @PathParam("domainId") String domainId) {
        return clientService.find(clientId).filter(client -> client.getDomain().equals(domainId))
                .map(client -> Response.ok(client).build()).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @GET
    @Path("/{domainId}/client")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientsByDomain(@PathParam("domainId") String domainId, @Rest QueryParameters queryParameters) {
        ResourceQuery query = queryParameters.getQuery().orElse(null);

        return queryParameters.getAggregation().map(aggregation -> {
            try {
                AggregationResult result = clientService.getClientsAggregation(domainId, query, aggregation);
                return Response.ok(result).build();

            } catch (InvalidAggregationException e) {
                return ErrorResponseFactory.getInstance().badRequest();
            }
        }

        ).orElseGet(() -> {
            Pagination pagination = queryParameters.getPagination();
            Sort sort = queryParameters.getSort().orElse(null);
            return Response.ok(clientService.findClientsByDomain(domainId, query, pagination, sort)).build();
        });
    }

    @PUT
    @Path("/{domainId}/client/{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyClient(@Valid Client client, @PathParam("clientId") String clientId, @PathParam("domainId") String domainId) {
        client.setDomain(domainId);
        client.setId(clientId);
        addTrace(client);
        clientService.update(client);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{domainId}/client/{clientId}")
    public Response deleteClient(@PathParam("clientId") String clientId, @PathParam("domainId") String domainId) {
        clientService.delete(domainId, clientId);
        return Response.noContent().build();
    }

    private void addTrace(TraceableEntity entity) {
        String hostName;

        try {
            hostName = Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "[error getting host name]";
        }

        entity.setCreatedBy("IamAPI on " + hostName);
        entity.setCreatedDate(new Date());
    }
}