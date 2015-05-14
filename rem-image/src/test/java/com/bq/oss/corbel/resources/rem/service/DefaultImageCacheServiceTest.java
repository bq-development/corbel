package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;

@RunWith(MockitoJUnitRunner.class) public class DefaultImageCacheServiceTest {

    private static final String COLLECTION_TEST = "test:Test";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    @Mock private RequestParameters<ResourceParameters> parameters;

    @Mock private Rem<InputStream> remMock;

    @Test
    public void getFromCacheTest() {
        Response responseMock = mock(Response.class);
        InputStream mockStreamResponse = mock(InputStream.class);
        when(responseMock.getStatus()).thenReturn(200);
        when(responseMock.getEntity()).thenReturn(mockStreamResponse);
        when(
                remMock.resource("images:ImageCache", new ResourceId(RESOURCE_ID.getId() + "." + COLLECTION_TEST + "." + "150x100"),
                        parameters, Optional.empty())).thenReturn(responseMock);

        DefaultImageCacheService defaultImageCacheService = new DefaultImageCacheService("images:ImageCache");
        assertThat(defaultImageCacheService.getFromCache(remMock, RESOURCE_ID, 150, 100, COLLECTION_TEST, parameters)).isEqualTo(
                mockStreamResponse);
    }

    @Test
    public void getFromCacheNotFoundTest() {
        Response responseMock = mock(Response.class);
        when(responseMock.getEntity()).thenReturn(null);
        when(
                remMock.resource("images:ImageCache", new ResourceId(RESOURCE_ID.getId() + "." + COLLECTION_TEST + "." + "150x100"),
                        parameters, Optional.empty())).thenReturn(responseMock);

        DefaultImageCacheService defaultImageCacheService = new DefaultImageCacheService("images:ImageCache");
        assertThat(defaultImageCacheService.getFromCache(remMock, RESOURCE_ID, 150, 100, COLLECTION_TEST, parameters)).isNull();
    }

    @Test
    public void putInCacheTest() throws FileNotFoundException {
        File mockFile = mock(File.class);
        InputStream mockStream = mock(FileInputStream.class);
        DefaultImageCacheService defaultImageCacheService = spy(new DefaultImageCacheService("images:ImageCache"));
        doReturn(mockStream).when(defaultImageCacheService).createInputStream(mockFile);
        defaultImageCacheService.saveInCacheAsync(remMock, RESOURCE_ID, 150, 100, 123123l, COLLECTION_TEST, parameters, mockFile);
        ArgumentCaptor<RequestParameters> argument = ArgumentCaptor.forClass(RequestParameters.class);
        verify(remMock).resource(eq("images:ImageCache"),
                eq(new ResourceId(RESOURCE_ID.getId() + "." + COLLECTION_TEST + "." + "150x100")), argument.capture(),
                eq(Optional.of(mockStream)));

        assertThat(argument.getValue().getContentLength()).isEqualTo(123123l);

    }
}
