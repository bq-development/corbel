package com.bq.oss.corbel.resources.rem;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
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

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.ImageOperationsService;
import com.bq.oss.corbel.resources.rem.service.RemService;

@RunWith(MockitoJUnitRunner.class) public class ImageGetRemTest {

    private static final String COLLECTION_TEST = "test:Test";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> parameters;
    @Mock private Rem restorRem;
    @Mock private ImageOperationsService imageOperationsService;
    @Mock private ImageCacheService imageCacheService;

    private ImageGetRem imageGetRem;
    @Mock private InputStream entity;

    @Before
    public void before() throws IOException {
        imageGetRem = new ImageGetRem(imageOperationsService, imageCacheService);
        imageGetRem.setRemService(remService);

        List<MediaType> mediaTypes = Collections.singletonList(MediaType.IMAGE_JPEG);
        when(parameters.getAcceptedMediaTypes()).thenReturn(mediaTypes);
        when(remService.getRem(COLLECTION_TEST, mediaTypes, HttpMethod.GET, Collections.singletonList(imageGetRem))).thenReturn(restorRem);

        when(restorRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty())).thenReturn(Response.ok(entity).build());
    }

    @Test
    public void resourceWidthTest() throws IOException, InterruptedException, IM4JavaException, ImageOperationsException {
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resizeWidth=250");
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isInstanceOf(StreamingOutput.class);
        assertThat(response.getStatus()).isEqualTo(200);
        OutputStream outputMock = mock(OutputStream.class);
        ((StreamingOutput) response.getEntity()).write(outputMock);

        verify(imageOperationsService).applyConversion(eq(Collections.singletonList(new ImageOperationDescription("resizeWidth", "250"))),
                eq(entity), any(TeeOutputStream.class));
        Thread.sleep(200);
        verify(imageCacheService).saveInCacheAsync(any(Rem.class), eq(RESOURCE_ID), eq("resizeWidth=250"), anyLong(), eq(COLLECTION_TEST),
                eq(parameters), any(File.class));
    }

    @Test
    public void resourceHeightTest() throws IOException, InterruptedException, IM4JavaException, ImageOperationsException {
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resize=(250, 150)");
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isInstanceOf(StreamingOutput.class);
        assertThat(response.getStatus()).isEqualTo(200);
        OutputStream outputMock = mock(OutputStream.class);
        ((StreamingOutput) response.getEntity()).write(outputMock);

        verify(imageOperationsService).applyConversion(
                eq(Collections.singletonList(new ImageOperationDescription("resize", "(250, 150)"))), eq(entity),
                any(TeeOutputStream.class));
        Thread.sleep(200);
        verify(imageCacheService).saveInCacheAsync(any(Rem.class), eq(RESOURCE_ID), eq("resize=(250, 150)"), anyLong(),
                eq(COLLECTION_TEST), eq(parameters), any(File.class));
    }

    @Test
    public void resourceWithoutParamentersTest() throws IOException, InterruptedException, IM4JavaException, ImageOperationsException {
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resize=(250, 150)");
        doThrow(IM4JavaException.class).when(imageOperationsService).applyConversion(any(), any(), any());
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);

        verify(imageCacheService).getFromCache(any(), eq(RESOURCE_ID), eq("resize=(250, 150)"), eq(COLLECTION_TEST), eq(parameters));
        verifyNoMoreInteractions(imageCacheService);
    }

    @Test
    public void resourceCacheTest() {
        InputStream mockStreamResponse = mock(InputStream.class);
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resize=(250, 150)");
        when(imageCacheService.getFromCache(restorRem, RESOURCE_ID, "resize=(250, 150)", COLLECTION_TEST, parameters)).thenReturn(
                mockStreamResponse);

        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isEqualTo(mockStreamResponse);

        verify(imageCacheService).getFromCache(restorRem, RESOURCE_ID, "resize=(250, 150)", COLLECTION_TEST, parameters);
        verifyNoMoreInteractions(imageCacheService);
    }

    @Test
    public void getParametersTest() throws ImageOperationsException {
        List<String> parametersStrings = Arrays.asList("resize=(12, 23);crop=(10, 10, 20, 20)", "  cropFromCenter  =  (10, 10)  ",
                ";crop=(10, 10, 20, 20);", "     ;resize=(      200     , 3  )   ");

        List<List<ImageOperationDescription>> expectedParameterLists = Arrays.asList(Arrays.asList(new ImageOperationDescription("resize",
                "(12, 23)"), new ImageOperationDescription("crop", "(10, 10, 20, 20)")), Collections
                .singletonList(new ImageOperationDescription("cropFromCenter", "(10, 10)")), Collections
                .singletonList(new ImageOperationDescription("crop", "(10, 10, 20, 20)")), Collections
                .singletonList(new ImageOperationDescription("resize", "(      200     , 3  )")));

        assertThat(parametersStrings.size()).isEqualTo(expectedParameterLists.size());

        for (int i = 0; i < parametersStrings.size(); ++i) {
            assertThat(imageGetRem.getParameters(parametersStrings.get(i))).isEqualTo(expectedParameterLists.get(i));
        }
    }
}
