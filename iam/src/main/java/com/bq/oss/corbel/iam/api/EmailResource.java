package com.bq.oss.corbel.iam.api;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path(ApiVersion.CURRENT + "/email")
public class EmailResource {

    private final UserService userService;

    public EmailResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdByEmail(@PathParam("email") String userEmail, @Auth AuthorizationInfo authorizationInfo) {
        return getUserInDomainByEmail(userEmail, authorizationInfo).map(user ->
                        Response.ok(user.getUserWithOnlyId()).build()
        ).orElseGet(() -> IamErrorResponseFactory.getInstance().notFound());
    }

    private Optional<User> getUserInDomainByEmail(String userEmail, AuthorizationInfo authorizationInfo) {
        return Optional.ofNullable(userService.findByDomainAndEmail(authorizationInfo.getDomainId(), userEmail));
    }
}
