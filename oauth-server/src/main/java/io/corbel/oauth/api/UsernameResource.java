package io.corbel.oauth.api;

import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.oauth.model.User;
import io.corbel.oauth.service.UserService;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Ricardo Martínez
 */
@Path(ApiVersion.CURRENT + "/username")
public class UsernameResource {
    private final UserService userService;

    public UsernameResource(UserService userService) {
        this.userService = userService;
    }

    @Path("/{username}")
    @HEAD
    public Response existUsername(@PathParam("username") String username, @Auth TokenReader token) {
        String domain = token.getInfo().getDomainId();
        return userService.existsByUsernameAndDomain(username, domain) ? Response.ok().build() : ErrorResponseFactory.getInstance()
                .notfound(new io.corbel.lib.ws.model.Error("not_found", "User " + username + " does not exist."));
    }

    @Path("/{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdByUsername(@PathParam("username") String username, @Auth TokenReader token) {
        return getUserInDomainByUsername(username, token).map(user ->
                        Response.ok(user.getUserWithOnlyId()).type(MediaType.APPLICATION_JSON_TYPE).build()
        ).orElseGet(() -> ErrorResponseFactory.getInstance().notfound
                (new io.corbel.lib.ws.model.Error("not_found", "User " + username + " does not exist.")));
    }

    private Optional<User> getUserInDomainByUsername(String username, TokenReader token) {
        String domain = token.getInfo().getDomainId();
        return Optional.ofNullable(userService.findByUserNameAndDomain(username, domain));
    }
}
