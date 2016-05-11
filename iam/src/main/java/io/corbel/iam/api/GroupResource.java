package io.corbel.iam.api;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.corbel.iam.exception.GroupAlreadyExistsException;
import io.corbel.iam.exception.NotExistentScopeException;
import io.corbel.iam.model.Group;
import io.corbel.iam.service.GroupService;
import io.corbel.lib.queries.jaxrs.QueryParameters;
import io.corbel.lib.ws.annotation.Rest;

@Path(ApiVersion.CURRENT + "/{domain}/group") public class GroupResource {

    private final GroupService groupService;

    public GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@PathParam("domain") String domain, @Rest QueryParameters queryParameters) {
        return Response
                .ok(groupService.getAll(domain, queryParameters.getQueries().orElseGet(Collections::emptyList),
                        queryParameters.getPagination(), queryParameters.getSort().orElse(null))).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("domain") String domain, @Context UriInfo uriInfo, @Valid Group group) {
        try {
            group.setDomain(domain);
            return Response.created(uriInfo.getAbsolutePathBuilder().path(groupService.create(group).getId()).build()).build();
        } catch (GroupAlreadyExistsException e) {
            return IamErrorResponseFactory.getInstance().groupAlreadyExists(e.getMessage());
        } catch (NotExistentScopeException e) {
            return IamErrorResponseFactory.getInstance().scopesNotExist(e.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response get(@PathParam("domain") String domain, @PathParam("id") final String id) {
        return groupService.getById(id, domain).map(group -> Response.ok(group).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

    @DELETE
    @Path("/{id}")
    public Response deleteGroup(@PathParam("domain") String domain, @PathParam("id") final String id) {
        return groupService.getById(id).map(group -> {
            if (!group.getDomain().equals(domain)) {
                return IamErrorResponseFactory.getInstance().unauthorizedGroupDeletion(id);
            }
            groupService.delete(id, domain);
            return Response.noContent().build();
        }).orElseGet(() -> Response.noContent().build());
    }

    @PUT
    @Path("/{id}/scope")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addScopes(@PathParam("domain") String domain, @PathParam("id") final String id, List<String> scopes) {
        return groupService.getById(id).map(group -> {
            if (!group.getDomain().equals(domain)) {
                return IamErrorResponseFactory.getInstance().unauthorizedGroupUpdate(id);
            }
            try {
                groupService.addScopes(id, scopes.stream().toArray(String[]::new));
            } catch (NotExistentScopeException e) {
                return IamErrorResponseFactory.getInstance().scopesNotExist(e.getMessage());
            }
            return Response.noContent().build();
        }).orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

    @DELETE
    @Path("/{id}/scope/{scopeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeScopes(@PathParam("domain") String domain, @PathParam("id") String id,
                                 @PathParam("scopeId") String scopeId) {
        return groupService.getById(id).map(group -> {
            if (!group.getDomain().equals(domain)) {
                return IamErrorResponseFactory.getInstance().unauthorizedGroupUpdate(id);
            }
            groupService.removeScopes(id, scopeId);
            return Response.noContent().build();
        }).orElseGet(() -> IamErrorResponseFactory.getInstance().groupNotExists(id));
    }

}
