package io.corbel.resources.rem.i18n;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.i18n.model.I18n;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.RemService;
import io.corbel.lib.queries.request.ResourceQuery;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class I18nGetRemTest {
    private static final String TEST_COLLECTION_TYPE = "I18n_TEST";
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    public static final String LANGUAGE = "es";
    private static final String TEST_ID = "TEST_ID";
    private static final String LARGE_LANGUAGE = "en,gb;q=0.5,es;q=0.2";

    private final I18nGetRem i18nGetRem = new I18nGetRem();
    private RemService remServiceMock;
    private RequestParameters requestParametersMock;
    private URI uriMock;
    private Rem remMock;
    private CollectionParameters collectionParametersMock;

    @Before
    public void setup() {
        remServiceMock = mock(RemService.class);
        requestParametersMock = mock(RequestParameters.class);
        uriMock = URI.create("");
        remMock = mock(Rem.class);
        collectionParametersMock = mock(CollectionParameters.class);
        i18nGetRem.setRemService(remServiceMock);
        List<MediaType> list = Arrays.asList(MediaType.APPLICATION_JSON);
        when(remServiceMock.getRem(anyString(), eq(list), eq(HttpMethod.GET))).thenReturn(remMock);
        when(requestParametersMock.getApiParameters()).thenReturn(collectionParametersMock);
    }

    @Test
    public void testGetCollection() {
        Response responseMock = mock(Response.class);

        when(responseMock.getStatus()).thenReturn(200);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject());
        when(responseMock.getEntity()).thenReturn(jsonArray);


        MultivaluedMap<String, String> multiValueMap = new MultivaluedHashMap();
        multiValueMap.add(ACCEPT_LANGUAGE_HEADER, LANGUAGE);
        ResourceQuery resourceQuery = new ResourceQuery();

        when(collectionParametersMock.getQueries()).thenReturn(Optional.of(Arrays.asList(resourceQuery)));

        when(requestParametersMock.getHeaders()).thenReturn(multiValueMap);
        when(remMock.collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.empty())).thenReturn(responseMock);

        Response response = i18nGetRem.collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.<I18n>empty());

        assertThat(response).isEqualTo(responseMock);
        verify(remMock).collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.empty());
        verify(collectionParametersMock).setQueries(any());
    }

    @Test
    public void testGetCollectionLargeLanguage() {
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(200);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject());
        when(responseMock.getEntity()).thenReturn(jsonArray);

        MultivaluedMap<String, String> multiValueMap = new MultivaluedHashMap();
        multiValueMap.add(ACCEPT_LANGUAGE_HEADER, LARGE_LANGUAGE);
        ResourceQuery resourceQuery = new ResourceQuery();

        when(collectionParametersMock.getQueries()).thenReturn(Optional.of(Arrays.asList(resourceQuery)));

        when(requestParametersMock.getHeaders()).thenReturn(multiValueMap);
        when(remMock.collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.empty())).thenReturn(responseMock);

        Response response = i18nGetRem.collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.<I18n>empty());

        assertThat(response).isEqualTo(responseMock);
        verify(remMock).collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.empty());
        ArgumentCaptor resourceQueryCaptor = ArgumentCaptor.forClass(Optional.class);
        verify(collectionParametersMock).setQueries((Optional<List<ResourceQuery>>) resourceQueryCaptor.capture());

        Optional<List<ResourceQuery>> resourceQueryCaptured = ((Optional<List<ResourceQuery>>) resourceQueryCaptor.getValue());

        assertThat((String) resourceQueryCaptured.get().get(0).iterator().next().getValue().getLiteral()).isEqualTo(
                LARGE_LANGUAGE.split(",")[0].split(";")[0] + ":");

    }

    @Test
    public void testGetCollectionNotLanguage() {
        when(requestParametersMock.getHeaders()).thenReturn(new MultivaluedHashMap());
        Response response = i18nGetRem.collection(TEST_COLLECTION_TYPE, requestParametersMock, uriMock, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetResource() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        ResourceId validResourceId = new ResourceId(LANGUAGE + ":" + TEST_ID);
        Response responseMock = mock(Response.class);
        when(responseMock.getStatus()).thenReturn(200);
        MultivaluedMap<String, String> multiValueMap = new MultivaluedHashMap();
        multiValueMap.add(ACCEPT_LANGUAGE_HEADER, LANGUAGE);

        when(requestParametersMock.getHeaders()).thenReturn(multiValueMap);
        when(remMock.resource(TEST_COLLECTION_TYPE, validResourceId, requestParametersMock, Optional.empty())).thenReturn(responseMock);

        Response response = i18nGetRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty());

        assertThat(response).isEqualTo(responseMock);
        verify(remMock).resource(TEST_COLLECTION_TYPE, validResourceId, requestParametersMock, Optional.empty());

    }

    @Test
    public void testGetResourceLargeLanguage() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        ResourceId validResourceId = new ResourceId("es:" + TEST_ID);
        Response responseMock200 = mock(Response.class);
        Response responseMock404 = mock(Response.class);

        when(responseMock200.getStatus()).thenReturn(200);
        when(responseMock404.getStatus()).thenReturn(404);

        MultivaluedMap<String, String> multiValueMap = new MultivaluedHashMap();
        multiValueMap.add(ACCEPT_LANGUAGE_HEADER, LARGE_LANGUAGE);

        when(requestParametersMock.getHeaders()).thenReturn(multiValueMap);

        when(remMock.resource(eq(TEST_COLLECTION_TYPE), not(eq(validResourceId)), eq(requestParametersMock), eq(Optional.empty())))
                .thenReturn(responseMock404);
        when(remMock.resource(TEST_COLLECTION_TYPE, validResourceId, requestParametersMock, Optional.empty())).thenReturn(responseMock200);

        Response response = i18nGetRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty());

        assertThat(response).isEqualTo(responseMock200);
        verify(remMock).resource(TEST_COLLECTION_TYPE, validResourceId, requestParametersMock, Optional.empty());
    }

    @Test
    public void testGetResourceNotLanguage() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        when(requestParametersMock.getHeaders()).thenReturn(new MultivaluedHashMap());
        Response response = i18nGetRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
