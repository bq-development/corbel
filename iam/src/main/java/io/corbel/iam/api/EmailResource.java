package io.corbel.iam.api;

import io.corbel.iam.model.User;
import io.corbel.iam.service.UserService;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path(ApiVersion.CURRENT + "/{domain}/email")
public class EmailResource {

    private final UserService userService;

    public EmailResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdByEmail(@PathParam("domain") String domain, @PathParam("email") String userEmail) {
        return getUserInDomainByEmail(userEmail, domain).map(user -> Response.ok(user.getUserWithOnlyId()).build())
                .orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    @HEAD
    @Path("/{email}")
    public Response existsUserByEmailInDomain(@PathParam("domain") String domain, @PathParam("email") String email) {
        return userService.existsByEmailAndDomain(email, domain) ? Response.ok().build() :
                IamErrorResponseFactory.getInstance().notFound();
    }

    private Optional<User> getUserInDomainByEmail(String userEmail, String domainId) {
        return Optional.ofNullable(userService.findByDomainAndEmail(domainId, userEmail));
    }
}
