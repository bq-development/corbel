package com.bq.oss.corbel.webfs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.amazonaws.services.s3.model.S3Object;
import com.bq.oss.corbel.webfs.service.AmazonS3Service;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;

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
    public Response getResource(@PathParam("path") String path) {
        S3Object object = amazonS3Service.getObject(path);
        if (object != null) {
            return Response.ok().type(object.getObjectMetadata().getContentType()).entity(object.getObjectContent()).build();
        }

        return ErrorResponseFactory.getInstance().notFound();
    }
}
