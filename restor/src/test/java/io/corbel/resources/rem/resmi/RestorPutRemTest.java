package io.corbel.resources.rem.resmi;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.restor.RestorPutRem;

/**
 * @author Alberto J. Rubio
 */
public class RestorPutRemTest {

	private RestorDao daoMock;
	private RequestParameters requestParameters;
	private InputStream entity;

	private RestorPutRem putRem;

	@Before
	public void setup() {
		daoMock = Mockito.mock(RestorDao.class);
		requestParameters = Mockito.mock(RequestParameters.class);
		entity = Mockito.mock(InputStream.class);
		putRem = new RestorPutRem(daoMock);
	}

	@Test
	public void testDeleteOkResource() {
		when(requestParameters.getAcceptedMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_XML));
		Response response = putRem.resource("test", new ResourceId("resourceId"), requestParameters,
				Optional.of(entity));
		assertThat(response.getStatus()).isEqualTo(204);
	}

	@Test
	public void testDeleteNullResource() {
		when(requestParameters.getAcceptedMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_XML));
		Response response = putRem.resource("test", new ResourceId("resourceId"), requestParameters, Optional.empty());
		assertThat(response.getStatus()).isEqualTo(400);
	}

}
