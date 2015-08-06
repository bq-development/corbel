package io.corbel.resources.rem.restor;

import java.io.InputStream;

import org.springframework.http.MediaType;

import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.dao.RestorDao;

/**
 * @author Alberto J. Rubio
 */
public abstract class AbstractRestorRem extends BaseRem<InputStream> {

	protected final RestorDao dao;

	public AbstractRestorRem(RestorDao dao) {
		this.dao = dao;
	}

	public MediaType getMediaType(RequestParameters<?> parameters) {
		return parameters.getAcceptedMediaTypes().get(0);
	}

	@Override
	public Class<InputStream> getType() {
		return InputStream.class;
	}
}
