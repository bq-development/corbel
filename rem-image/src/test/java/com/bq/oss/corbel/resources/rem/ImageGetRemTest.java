package com.bq.oss.corbel.resources.rem;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.TeeOutputStream;
import org.im4java.core.IM4JavaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.corbel.resources.rem.service.ResizeImageService;

@RunWith(MockitoJUnitRunner.class) public class ImageGetRemTest {

    private static final long CONTENT_LENGTH = 111l;
    private static final String COLLECTION_TEST = "test:Test";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> parameters;
    @Mock private Rem restorRem;
    @Mock private ResizeImageService resizeImageService;
    @Mock private ImageCacheService imageCacheService;

    private ImageGetRem imageGetRem;
    @Mock private InputStream entity;

    @Before
    public void before() throws IOException {
        imageGetRem = new ImageGetRem(resizeImageService, imageCacheService);
        imageGetRem.setRemService(remService);

        List<MediaType> mediaTypes = Arrays.asList(MediaType.IMAGE_JPEG);
        when(parameters.getAcceptedMediaTypes()).thenReturn(mediaTypes);
        when(remService.getRem(COLLECTION_TEST, mediaTypes, HttpMethod.GET, Arrays.asList(imageGetRem))).thenReturn(restorRem);

        when(restorRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty())).thenReturn(Response.ok(entity).build());
    }

    @Test
    public void resourceWidthTest() throws IOException, InterruptedException, IM4JavaException {
        when(parameters.getCustomParameterValue("image:width")).thenReturn("250");
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isInstanceOf(StreamingOutput.class);
        assertThat(response.getStatus()).isEqualTo(200);
        OutputStream outputMock = mock(OutputStream.class);
        ((StreamingOutput) response.getEntity()).write(outputMock);

        verify(resizeImageService).resizeImage(eq(entity), eq(250), isNull(Integer.class), any(TeeOutputStream.class));
        Thread.sleep(200);
        verify(imageCacheService).saveInCacheAsync(any(Rem.class), eq(RESOURCE_ID), eq(250), isNull(Integer.class), any(),
                eq(COLLECTION_TEST), eq(parameters), any(File.class));
    }

    @Test
    public void resourceHeightTest() throws IOException, InterruptedException, IM4JavaException {
        when(parameters.getCustomParameterValue("image:width")).thenReturn("250");
        when(parameters.getCustomParameterValue("image:height")).thenReturn("150");
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isInstanceOf(StreamingOutput.class);
        assertThat(response.getStatus()).isEqualTo(200);
        OutputStream outputMock = mock(OutputStream.class);
        ((StreamingOutput) response.getEntity()).write(outputMock);

        verify(resizeImageService).resizeImage(eq(entity), eq(250), eq(150), any(TeeOutputStream.class));
        Thread.sleep(200);
        verify(imageCacheService).saveInCacheAsync(any(Rem.class), eq(RESOURCE_ID), eq(250), eq(150), any(), eq(COLLECTION_TEST),
                eq(parameters), any(File.class));
    }

    @Test
    public void resourceWithoutParamentersTest() throws IOException, InterruptedException, IM4JavaException {
        doThrow(IM4JavaException.class).when(resizeImageService).resizeImage(any(), any(), any(), any());
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(entity);

        verify(imageCacheService, never()).saveInCacheAsync(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void resourceCacheTest() {
        InputStream mockStreamResponse = mock(InputStream.class);
        when(parameters.getCustomParameterValue("image:width")).thenReturn("250");
        when(parameters.getCustomParameterValue("image:height")).thenReturn("150");
        when(imageCacheService.getFromCache(restorRem, RESOURCE_ID, 250, 150, COLLECTION_TEST, parameters)).thenReturn(mockStreamResponse);

        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isEqualTo(mockStreamResponse);

        verify(imageCacheService, never()).saveInCacheAsync(any(), any(), any(), any(), any(), any(), any(), any());
    }
}
