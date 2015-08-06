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
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.i18n.model.I18n;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.RemService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class I18nPutRemTest {
    private static final String TEST_COLLECTION_TYPE = "I18n_TEST";
    public static final String LANGUAGE = "es";
    private static final String TEST_ID = "TEST_ID";
    private static final String TEST_KEY = "TEST_KEY";
    private static final String TEST_VALUE = "TEST_VALUE";

    private I18nPutRem i18nPutRem;
    private RemService remServiceMock;
    private RequestParameters requestParametersMock;
    private Rem remMock;

    I18n i18nTestModel = new I18n();
    Gson gson = new Gson();

    @Before
    public void setup() {
        i18nTestModel.setLang(LANGUAGE).setValue(TEST_VALUE);
        i18nPutRem = new I18nPutRem(gson);
        remServiceMock = mock(RemService.class);
        requestParametersMock = mock(RequestParameters.class);
        remMock = mock(Rem.class);
        i18nPutRem.setRemService(remServiceMock);
        List<MediaType> list = Arrays.asList(MediaType.APPLICATION_JSON);
        when(remServiceMock.getRem(anyString(), eq(list), eq(HttpMethod.PUT))).thenReturn(remMock);
    }

    @Test
    public void testPutResource() {
        ResourceId resourceId = new ResourceId(TEST_KEY);
        Response responseMock = mock(Response.class);

        Optional<JsonElement> i18nCompleted = Optional.of(gson.toJsonTree(new I18n().setId(LANGUAGE + ":" + TEST_KEY).setKey(TEST_KEY)
                .setLang(LANGUAGE).setValue(TEST_VALUE)));

        when(remMock.resource(eq(TEST_COLLECTION_TYPE), eq(resourceId), eq(requestParametersMock), eq(i18nCompleted))).thenReturn(
                responseMock);

        Response response = i18nPutRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, Optional.of(i18nTestModel));

        assertThat(response).isEqualTo(responseMock);
        verify(remMock).resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock, i18nCompleted);
        assertThat(resourceId.getId()).isEqualTo(LANGUAGE + ":" + TEST_KEY);
    }

    @Test
    public void testPutResourceNotLanguage() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        when(requestParametersMock.getHeaders()).thenReturn(new MultivaluedHashMap());
        Response response = i18nPutRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock,
                Optional.of(i18nTestModel.setLang(null)));
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void testPutResourceNotValue() {
        ResourceId resourceId = new ResourceId(TEST_ID);
        when(requestParametersMock.getHeaders()).thenReturn(new MultivaluedHashMap());
        Response response = i18nPutRem.resource(TEST_COLLECTION_TYPE, resourceId, requestParametersMock,
                Optional.of(i18nTestModel.setValue(null)));
        assertThat(response.getStatus()).isEqualTo(422);
    }
}
