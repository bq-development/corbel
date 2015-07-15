package com.bq.oss.corbel.resources.rem.service;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.format.ImageFormat;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.operation.ImageOperation;
import com.google.common.collect.ImmutableMap;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultImageOperationsServiceTest {

    @Mock
    private static ImageOperation imageOperationMock;
    @Mock
    private DefaultImageOperationsService.IMOperationFactory imOperationFactory;
    @Mock
    private DefaultImageOperationsService.ConvertCmdFactory convertCmdFactory;
    @Mock
    private static ImageOperation ImageOperationMock;
    @Mock
    private static InputStream image = mock(InputStream.class);
    private static OutputStream out = mock(OutputStream.class);
    ;
    private static IMOperation imOperation;
    private static ConvertCmd convertCmd = mock(ConvertCmd.class);
    private static IMOperation imOperationMock = mock(IMOperation.class);
    private Map<String, ImageOperation> operations;
    private DefaultImageOperationsService defaultImageOperationsService;
    private Optional<ImageFormat> imageFormat = Optional.of(ImageFormat.PNG);
    private Optional<ImageFormat> imageFormatNull = Optional.empty();

    @Before
    public void setUp() throws ImageOperationsException {
        operations = ImmutableMap.<String, ImageOperation>builder().put("resizeWidth", imageOperationMock).build();
        defaultImageOperationsService = new DefaultImageOperationsService(imOperationFactory, convertCmdFactory, operations);
        imOperation = mock(IMOperation.class);

        when(imOperationFactory.create()).thenReturn(imOperation);
        when(convertCmdFactory.create(any(), any())).thenReturn(convertCmd);
        when(ImageOperationMock.apply(any())).thenReturn(imOperationMock);
    }

    @Test
    public void applyConversionTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        List<ImageOperationDescription> parameters = Collections.singletonList(new ImageOperationDescription("resizeWidth", "10"));

        defaultImageOperationsService.applyConversion(parameters, image, out, imageFormatNull);

        verify(imOperationFactory).create();
        verify(imOperation, times(2)).addImage(eq("-"));

        ArgumentCaptor<IMOperation> capturedIMOperation = ArgumentCaptor.forClass(IMOperation.class);
        verify(imOperation).addSubOperation(capturedIMOperation.capture());

        verify(convertCmdFactory).create(any(), any());
        verify(convertCmd).run(imOperation);

    }

    @Test
    public void applyConversionWithFormatTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        List<ImageOperationDescription> parameters = Collections.emptyList();

        defaultImageOperationsService.applyConversion(parameters, image, out, imageFormat);

        verify(imOperationFactory).create();
        verify(imOperation, times(1)).addImage(eq("-"));
        verify(imOperation, times(1)).addImage("PNG:-");

        ArgumentCaptor<IMOperation> capturedIMOperation = ArgumentCaptor.forClass(IMOperation.class);
        verify(imOperation, times(0)).addSubOperation(capturedIMOperation.capture());

        verify(convertCmdFactory).create(any(), any());
        verify(convertCmd).run(imOperation);
    }

    @Test(expected = ImageOperationsException.class)
    public void applyConversionWithUnknownOperationTest() throws InterruptedException, IOException, IM4JavaException, ImageOperationsException {
        List<ImageOperationDescription> parameters = Collections.singletonList(new ImageOperationDescription("gaussianBlur", "10"));

        try {
            defaultImageOperationsService.applyConversion(parameters, image, out, imageFormatNull);
        } catch (ImageOperationsException e) {
            verify(imOperationFactory).create();
            verify(imOperation, times(1)).addImage(eq("-"));
            throw e;
        }
    }

    @Test(expected = ImageOperationsException.class)
    public void applyConversionWithUnknownOperationButFormatTest() throws InterruptedException, IOException, IM4JavaException, ImageOperationsException {
        List<ImageOperationDescription> parameters = Collections.singletonList(new ImageOperationDescription("gaussianBlur", "10"));

        try {
            defaultImageOperationsService.applyConversion(parameters, image, out, imageFormat);
        } catch (ImageOperationsException e) {
            verify(imOperationFactory).create();
            verify(imOperation, times(1)).addImage(eq("-"));
            verify(imOperation, times(0)).addImage("PNG:-");
            throw e;
        }
    }

    @After
    public void afterTesting() {
        verifyNoMoreInteractions(imOperationFactory, convertCmdFactory, imOperation, convertCmd, image, out);
    }
}
