package io.corbel.oauth.api;

import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.Role;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.CreateUserException;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.UserService;
import io.dropwizard.auth.Auth;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * @author Francisco Sanchez
 */
@Path(ApiVersion.CURRENT + "/user") public class UserResource {

    private static final String ME = "me";
    private final UserService userService;
    private final ClientService clientService;

    public UserResource(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Context UriInfo uriInfo, @Auth Client client, @Valid User user) {
        try {
            user.setRole(Role.USER);
            user.setId(null);
            String id = userService.createUser(user, client);
            return Response.created(uriInfo.getAbsolutePathBuilder().path(id).build()).build();
        } catch (CreateUserException.DuplicatedUser duplicatedUser) {
            return ErrorResponseFactory.getInstance().conflict(new Error("entity_exists", "User already exists"));
        }
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String id, @Auth TokenReader token) {
        User user = getUserFromIdAliases(id, token);
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(user.getUser()).build();
    }

    @Path("/{id}/profile")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfile(@PathParam("id") String id, @Auth TokenReader token) {
        User user = getUserFromIdAliases(id, token);
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(user.getUserProfile()).build();
    }

    @Path("/{id}/avatar")
    @GET
    public Response getAvatar(@PathParam("id") String id, @Auth TokenReader token) {
        User user = getUserFromIdAliases(id, token);

        return Optional.ofNullable(user.getAvatarUri()).map(avatarUriAsString -> {
            try {
                return new URI(avatarUriAsString);
            } catch (URISyntaxException e) {
                return null;
            }
        }).map(avatarUri -> Response.temporaryRedirect(avatarUri).build())
                .orElseGet(() -> ErrorResponseFactory.getInstance()
                        .notfound(new Error("not_found", "User " + id + " has no avatar.")));
    }

    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, @Auth TokenReader token, User userUpdatedData) {
        if (userUpdatedData == null) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", "Invalid update data"));
        }
        try {
            // We don't allow updating email validation status via this endpoint
            Client client = clientService.findByName(token.getInfo().getClientId())
                    .orElseThrow(() -> new WebApplicationException(ErrorResponseFactory.getInstance().unauthorized()));
            userUpdatedData.setEmailValidated(null);
            User user = getUserFromIdAliases(id, token);
            checkUpdateUserRolePermissions(token.getInfo().getUserId(), userUpdatedData.getRole());
            userService.updateUser(user, userUpdatedData, client);

            return Response.noContent().build();
        } catch (CreateUserException.DuplicatedUser duplicatedUser) {
            return ErrorResponseFactory.getInstance().conflict(new Error("entity_exists", "User already exists"));
        }
    }

    @Path("/{id}/emailConfirmation")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmEmail(@PathParam("id") String id, @Auth TokenReader token) {
        // Email address is expected in token state.
        String state = token.getInfo().getState();
        if (state == null) {
            return ErrorResponseFactory.getInstance().badRequest();
        }
        userService.confirmEmail(state);
        return Response.noContent().build();
    }

    @Path("/{id}")
    @DELETE
    public Response delete(@PathParam("id") String id, @Auth TokenReader token) {
        User user = getUserFromIdAliases(id, token);
        userService.deleteUser(user.getId());
        return Response.noContent().build();
    }

    @Path("/{id}/validate")
    @GET
    public Response generateValidationEmail(@PathParam("id") String id, @Auth TokenReader token) {
        User user = getUserFromIdAliases(id, token);
        return clientService.findByName(token.getInfo().getClientId()).map(client -> {
            userService.sendValidationEmail(user, client);
            return Response.ok().build();
        }).orElse(ErrorResponseFactory.getInstance().notFound());
    }

    @Path("/resetPassword")
    @GET
    public Response generateResetPasswordEmail(@Auth Client client, @QueryParam("email") String email) {
        userService.sendMailResetPassword(email, client);
        return Response.noContent().build();
    }

    private User getUserFromIdAliases(String id, TokenReader token) {
        String userId = token.getInfo().getUserId();
        User authenticatedUser = userService.getUser(userId);
        if (ME.equals(id) || id.equals(userId)) {
            return authenticatedUser;
        }
        User user = userService.getUser(id);
        if (user != null && authenticatedUser.getRole().canUpdate(user.getRole())
                && user.getDomain().equals(authenticatedUser.getDomain())) {
            return user;
        }
        throw new WebApplicationException(ErrorResponseFactory.getInstance().notFound());
    }

    private void checkUpdateUserRolePermissions(String userId, Role role) {
        if (role != null) {
            User authenticatedUser = userService.getUser(userId);
            if (!authenticatedUser.getRole().canChangeRoleTo(role)) {
                throw new WebApplicationException(ErrorResponseFactory.getInstance().forbidden());
            }
        }
    }
}
