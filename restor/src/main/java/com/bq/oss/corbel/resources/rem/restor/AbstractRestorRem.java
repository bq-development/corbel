package com.bq.oss.corbel.resources.rem.restor;

import java.io.InputStream;

import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.BaseRem;
import com.bq.oss.corbel.resources.rem.dao.RestorDao;

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
