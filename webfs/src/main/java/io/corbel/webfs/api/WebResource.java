package io.corbel.webfs.api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.amazonaws.services.s3.model.S3Object;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.webfs.service.AmazonS3Service;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.dropwizard.auth.Auth;

/**
 * @author Rubén Carrasco
 *
 */

@Path(ApiVersion.CURRENT + "/{domain}/path") public class WebResource {

    private final AmazonS3Service amazonS3Service;

    public WebResource(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    @GET
    @Path("/{path: .*}")
    public Response getResource(@PathParam("domain") String domain, @PathParam("path") String path,
                                @HeaderParam("Accept") String accept) {

        S3Object object = amazonS3Service.getObject(domain +"/"+ path);
        if (object != null) {
            if (accept==null || accept.startsWith("*/*")) {
                accept = object.getObjectMetadata().getContentType();
            } else {
                accept = accept.split(",")[0].split(";")[0];
            }
            return Response.ok().type(accept).entity(object.getObjectContent()).build();
        }

        return ErrorResponseFactory.getInstance().notFound();
    }

    @DELETE
    @Path("/{path: .*}")
    public Response deleteFolder(@PathParam("domain") String domain, @PathParam("path") String path ){
        amazonS3Service.deleteFolder(domain +"/"+ path);
        return Response.noContent().build();
    }
}
