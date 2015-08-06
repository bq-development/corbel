package io.corbel.iam.api;

import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.corbel.iam.exception.ScopeNameException;
import io.corbel.iam.model.Scope;
import io.corbel.iam.service.ScopeService;

@Path(ApiVersion.CURRENT + "/scope") public class ScopeResource {

    private final ScopeService scopeService;

    public ScopeResource(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response createScope(@Context UriInfo uriInfo, @Valid Scope scope) {
        try {
            scopeService.create(scope);
        } catch (ScopeNameException e) {
            return IamErrorResponseFactory.getInstance().scopeIdNotAllowed(scope.getId());
        }

        return Response.created(uriInfo.getAbsolutePathBuilder().path(scope.getId()).build()).build();
    }

    @GET
    @Path("/{scopeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response getScope(@PathParam("scopeId") String scopeId) {
        return Optional.ofNullable(scopeService.getScope(scopeId)).map(scope -> Response.ok(scope).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @DELETE
    @Path("/{scopeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response deleteScope(@PathParam("scopeId") String scope) {
        scopeService.delete(scope);
        return Response.noContent().build();
    }
}
