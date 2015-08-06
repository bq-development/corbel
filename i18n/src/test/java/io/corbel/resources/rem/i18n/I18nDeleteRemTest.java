package io.corbel.resources.rem.i18n;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.RemService;

public class I18nDeleteRemTest {
    private static final String TEST_COLLECTION_TYPE = "I18n_TEST";
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    public static final String LANGUAGE = "es";
    private static final String TEST_ID = "TEST_ID";

    private final I18nDeleteRem i18DeleteRem = new I18nDeleteRem();
    private RemService remServiceMock;
    private RequestParameters requestParametersMock;
    private Rem remMock;
    private CollectionParameters collectionParametersMock;

    @Before
    public void setup() {
        remServiceMock = mock(RemService.class);
        requestParametersMock = mock(RequestParameters.class);
        remMock = mock(Rem.class);
        collectionParametersMock = mock(CollectionParameters.class);
        i18DeleteRem.setRemService(remServiceMock);
        List<MediaType> list = Arrays.asList(MediaType.APPLICATION_JSON);
        when(remServiceMock.getRem(anyString(), eq(list), eq(HttpMethod.DELETE))).thenReturn(remMock);
        when(requestParametersMock.getApiParameters()).thenReturn(collectionParametersMock);
    }

    @Test
    public void testDeleteResource() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        Response responseMock = mock(Response.class);
        MultivaluedMap<String, String> multiValueMap = new MultivaluedHashMap();
        multiValueMap.add(ACCEPT_LANGUAGE_HEADER, LANGUAGE);

        when(requestParametersMock.getHeaders()).thenReturn(multiValueMap);
        when(remMock.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty())).thenReturn(responseMock);

        Response response = i18DeleteRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty());

        assertThat(response).isEqualTo(responseMock);
        verify(remMock).resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty());
        assertThat(resourceId.getId()).isEqualTo(LANGUAGE + ":" + TEST_ID);

    }

    @Test
    public void testDeleteResourceNotLanguage() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        when(requestParametersMock.getHeaders()).thenReturn(new MultivaluedHashMap());
        Response response = i18DeleteRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
