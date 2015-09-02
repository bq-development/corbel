package io.corbel.resources.rem;

import io.corbel.resources.rem.exception.ImageOperationsException;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.model.ImageOperationDescription;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.ImageCacheService;
import io.corbel.resources.rem.service.ImageOperationsService;
import io.corbel.resources.rem.service.RemService;
import org.apache.commons.io.output.TeeOutputStream;
import org.im4java.core.IM4JavaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ImageGetRemTest {

    private static final String RESTOR = "RestorGetRem";
    private static final String COLLECTION_TEST = "test:Test";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");

    @Mock
    private RemService remService;
    @Mock
    private RequestParameters<ResourceParameters> parameters;
    @Mock
    private Rem restorRem;
    @Mock
    private ImageOperationsService imageOperationsService;
    @Mock
    private ImageCacheService imageCacheService;

    private ImageGetRem imageGetRem;
    @Mock
    private InputStream entity;
    private static String IM_MEMORY_LIMIT = "200MiB";


    @Before
    public void before() throws IOException, ImageOperationsException {
        imageGetRem = new ImageGetRem(imageOperationsService, imageCacheService, IM_MEMORY_LIMIT);
        imageGetRem.setRemService(remService);

        List<MediaType> mediaTypes = Collections.singletonList(MediaType.IMAGE_JPEG);
        when(parameters.getAcceptedMediaTypes()).thenReturn(mediaTypes);
        when(remService.getRem(RESTOR)).thenReturn(restorRem);
        when(restorRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty())).thenReturn(Response.ok(entity).build());
    }

    @Test
    public void imageFormatTest() throws IOException, ImageOperationsException, InterruptedException, IM4JavaException {
        when(parameters.getCustomParameterValue(ImageGetRem.FORMAT_PARAMETER)).thenReturn("png");
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isInstanceOf(StreamingOutput.class);
        assertThat(response.getStatus()).isEqualTo(200);
        OutputStream outputMock = mock(OutputStream.class);
        ((StreamingOutput) response.getEntity()).write(outputMock);

        verify(imageOperationsService).applyConversion(anyListOf(ImageOperationDescription.class),
                eq(entity), any(TeeOutputStream.class), any(Optional.class), any());
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
                eq(entity), any(TeeOutputStream.class), any(Optional.class), any());
        Thread.sleep(200);
        verify(imageCacheService).saveInCacheAsync(any(Rem.class), eq(RESOURCE_ID), eq("resizeWidth=250"), any(), anyLong(), eq(COLLECTION_TEST),
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
                any(TeeOutputStream.class), any(Optional.class), any());
        Thread.sleep(200);
        verify(imageCacheService).saveInCacheAsync(any(Rem.class), eq(RESOURCE_ID), eq("resize=(250, 150)"), any(), anyLong(),
                eq(COLLECTION_TEST), eq(parameters), any(File.class));
    }

    @Test
    public void resourceWithoutParamentersTest() throws IOException, InterruptedException, IM4JavaException, ImageOperationsException {
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resize=(250, 150)");
        doThrow(IM4JavaException.class).when(imageOperationsService).applyConversion(any(), any(), any(), any(), any());
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getStatus()).isEqualTo(200);

        verify(imageCacheService).getFromCache(any(), eq(RESOURCE_ID), eq("resize=(250, 150)"), any(), eq(COLLECTION_TEST), eq(parameters));
        verifyNoMoreInteractions(imageCacheService);
    }

    @Test
    public void resourceCacheTest() throws ImageOperationsException {
        InputStream mockStreamResponse = mock(InputStream.class);
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resize=(250, 150)");
        when(parameters.getCustomParameterValue(ImageGetRem.FORMAT_PARAMETER)).thenReturn(null);
        when(imageCacheService.getFromCache(eq(restorRem), eq(RESOURCE_ID), eq("resize=(250, 150)"), any(), eq(COLLECTION_TEST), eq(parameters))).thenReturn(
                mockStreamResponse);

        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isEqualTo(mockStreamResponse);

        ArgumentCaptor<Optional> argumentCaptor = ArgumentCaptor.forClass(Optional.class);
        verify(imageCacheService).getFromCache(eq(restorRem), eq(RESOURCE_ID), eq("resize=(250, 150)"), argumentCaptor.capture(), eq(COLLECTION_TEST), eq(parameters));
        assertThat(argumentCaptor.getValue().isPresent()).isFalse();
        verifyNoMoreInteractions(imageCacheService);
    }

    @Test
    public void resourceCacheTestWithFormat() throws ImageOperationsException {
        InputStream mockStreamResponse = mock(InputStream.class);
        when(parameters.getCustomParameterValue(ImageGetRem.OPERATIONS_PARAMETER)).thenReturn("resize=(250, 150)");
        when(parameters.getCustomParameterValue(ImageGetRem.FORMAT_PARAMETER)).thenReturn("png");
        when(imageCacheService.getFromCache(eq(restorRem), eq(RESOURCE_ID), eq("resize=(250, 150)"), any(), eq(COLLECTION_TEST), eq(parameters))).thenReturn(
                mockStreamResponse);

        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        assertThat(response.getEntity()).isEqualTo(mockStreamResponse);

        ArgumentCaptor<Optional> argumentCaptor = ArgumentCaptor.forClass(Optional.class);
        verify(imageCacheService).getFromCache(eq(restorRem), eq(RESOURCE_ID), eq("resize=(250, 150)"), argumentCaptor.capture(), eq(COLLECTION_TEST), eq(parameters));
        assertThat(argumentCaptor.getValue().isPresent()).isTrue();
        assertThat(argumentCaptor.getValue().get()).isEqualTo(ImageFormat.PNG);
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
