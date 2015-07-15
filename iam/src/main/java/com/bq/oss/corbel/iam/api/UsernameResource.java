package com.bq.oss.corbel.iam.api;

import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import io.dropwizard.auth.Auth;

import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
}
