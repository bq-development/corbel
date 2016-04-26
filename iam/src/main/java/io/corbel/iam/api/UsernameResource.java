package io.corbel.iam.api;

import io.corbel.iam.model.User;
import io.corbel.iam.service.UserService;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Francisco Sanchez
 */
@Path(ApiVersion.CURRENT + "/{domain}/username")
public class UsernameResource {

    private final UserService userService;

    public UsernameResource(UserService userService) {
        this.userService = userService;
    }

    @Path("/{username}")
    @HEAD
    public Response existUsername(@PathParam("domain") String domain, @PathParam("username") String username) {
        return userService.existsByUsernameAndDomain(username, domain) ? Response.ok().build() :
                IamErrorResponseFactory.getInstance().notFound();
    }

    @Path("/{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdByUsername(@PathParam("domain") String domain, @PathParam("username") String username) {
        return getUserInDomainByUsername(username, domain).map(user ->
                        Response.ok(user.getUserWithOnlyId()).build()
        ).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    private Optional<User> getUserInDomainByUsername(String username, String domain) {
        return Optional.ofNullable(userService.findByDomainAndUsername(domain, username));
    }
}
