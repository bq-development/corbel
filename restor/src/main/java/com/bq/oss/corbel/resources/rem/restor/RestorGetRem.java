package com.bq.oss.corbel.resources.rem.restor;

import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.Response;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.dao.RestorDao;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.model.RestorObject;

/**
 * @author Rubén Carrasco
 */
public class RestorGetRem extends AbstractRestorRem {

	public RestorGetRem(RestorDao dao) {
		super(dao);
	}

	@Override
	public Response resource(String collection, ResourceId resource, RequestParameters<ResourceParameters> parameters,
			Optional<InputStream> entity) {
		RestorObject object = dao.getObject(getMediaType(parameters), collection, resource.getId());
		if (object != null) {
			return Response.ok().type(object.getMediaType().toString()).entity(object.getInputStream()).build();
		}
		return ErrorResponseFactory.getInstance().notFound();
	}
}
