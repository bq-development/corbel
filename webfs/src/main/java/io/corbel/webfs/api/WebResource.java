package io.corbel.webfs.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.services.s3.model.S3Object;
import io.corbel.webfs.service.AmazonS3Service;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;

import java.util.Optional;

/**
 * @author Rubén Carrasco
 *
 */

@Path(ApiVersion.CURRENT) public class WebResource {

    private final AmazonS3Service amazonS3Service;

    public WebResource(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    @GET
    @Path("/{path: .*}")
    public Response getResource(@PathParam("path") String path, @HeaderParam("Accept") String accept) {
        S3Object object = amazonS3Service.getObject(path);
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
}
