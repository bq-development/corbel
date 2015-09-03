package io.corbel.resources.rem.resmi;

import io.corbel.resources.rem.dao.RestorDao;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.restor.RestorDeleteRem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Alberto J. Rubio
 */
public class RestorDeleteRemTest {

	private RestorDao daoMock;
	private RequestParameters requestParameters;

	private RestorDeleteRem deleteRem;

	@Before
	public void setup() {
		daoMock = Mockito.mock(RestorDao.class);
		requestParameters = Mockito.mock(RequestParameters.class);
		deleteRem = new RestorDeleteRem(daoMock);
	}

	@Test
	public void testDeleteOkResource() {
		when(requestParameters.getAcceptedMediaTypes()).thenReturn(Arrays.asList(MediaType.APPLICATION_XML));
		Response response = deleteRem.resource("test", new ResourceId("resourceId"), requestParameters,
				Optional.empty());
		assertThat(response.getStatus()).isEqualTo(204);
	}
}
