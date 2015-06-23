package com.bq.oss.corbel.resources.rem.restor;

import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.dao.RestorDao;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;

/**
 * @author Rubén Carrasco
 */
public class RestorDeleteRem extends AbstractRestorRem {

	public RestorDeleteRem(RestorDao dao) {
		super(dao);
	}

	@Override
	public Response resource(String collection, ResourceId resource, RequestParameters<ResourceParameters> parameters,
			Optional<InputStream> entity) {
		dao.deleteObject(getMediaType(parameters), collection, resource.getId());
		return Response.noContent().build();
	}

}
