package io.corbel.resources.api.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider public class IllegalArgumentHandler implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException e) {
        return Response.status(Status.BAD_REQUEST).entity(new io.corbel.lib.ws.model.Error("bad_request", e.getMessage())).build();
    }
}
