package com.bq.oss.corbel.iam.api;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.service.UserService;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Francisco Sanchez
 */
@Path(ApiVersion.CURRENT + "/username")
public class UsernameResource {

    private final UserService userService;

    public UsernameResource(UserService userService) {
        this.userService = userService;
    }

    @Path("/{username}")
    @HEAD
    public Response existUsername(@PathParam("username") String username, @Auth AuthorizationInfo authorizationInfo) {
        String domain = authorizationInfo.getTokenReader().getInfo().getDomainId();
        return userService.existsByUsernameAndDomain(username, domain) ? Response.ok().build() : IamErrorResponseFactory.getInstance()
                .notFound();
    }

    @Path("/{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdByUsername(@PathParam("username") String username, @Auth AuthorizationInfo authorizationInfo) {
        return getUserInDomainByUsername(username, authorizationInfo).map(user ->
                        Response.ok(user.getUserWithOnlyId()).build()
        ).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    private Optional<User> getUserInDomainByUsername(String username, AuthorizationInfo authorizationInfo) {
        return Optional.ofNullable(userService.findByDomainAndUsername(authorizationInfo.getDomainId(), username));
    }
}
