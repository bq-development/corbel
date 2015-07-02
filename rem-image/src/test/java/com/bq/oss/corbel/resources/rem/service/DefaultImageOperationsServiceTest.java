package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.operation.ImageOperation;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class) public class DefaultImageOperationsServiceTest {

    @Mock private static ImageOperation imageOperationMock;
    private Map<String, ImageOperation> operations;
    @Mock private DefaultImageOperationsService.IMOperationFactory imOperationFactory;
    @Mock private DefaultImageOperationsService.ConvertCmdFactory convertCmdFactory;
    private DefaultImageOperationsService defaultImageOperationsService;

    @Before
    public void setUp() {
        operations = ImmutableMap.<String, ImageOperation>builder().put("resizeWidth", imageOperationMock).build();
        defaultImageOperationsService = new DefaultImageOperationsService(imOperationFactory, convertCmdFactory, operations);
    }

    @Test
    public void applyConversionTest() throws InterruptedException, IM4JavaException, ImageOperationsException, IOException {
        InputStream image = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);
        IMOperation imOperation = mock(IMOperation.class);
        ConvertCmd convertCmd = mock(ConvertCmd.class);
        IMOperation imOperationMock = mock(IMOperation.class);

        List<ImageOperationDescription> parameters = Collections.singletonList(new ImageOperationDescription("resizeWidth", "10"));

        when(imOperationFactory.create()).thenReturn(imOperation);
        when(convertCmdFactory.create(any(), any())).thenReturn(convertCmd);
        when(imageOperationMock.apply(any())).thenReturn(imOperationMock);
        defaultImageOperationsService.applyConversion(parameters, image, out);

        verify(imOperationFactory).create();
        verify(imOperation, times(2)).addImage(eq("-"));

        ArgumentCaptor<IMOperation> capturedIMOperation = ArgumentCaptor.forClass(IMOperation.class);
        verify(imOperation).addSubOperation(capturedIMOperation.capture());
        assertThat(capturedIMOperation.getValue()).isEqualTo(imOperationMock);

        verify(convertCmdFactory).create(any(), any());
        verify(convertCmd).run(imOperation);

        verifyNoMoreInteractions(imOperationFactory, convertCmdFactory, imOperation, convertCmd, image, out);
    }

    @Test(expected = ImageOperationsException.class)
    public void applyConversionWithUnknownOperationTest() throws InterruptedException, IOException, IM4JavaException,
            ImageOperationsException {
        InputStream image = mock(InputStream.class);
        OutputStream out = mock(OutputStream.class);
        IMOperation imOperation = mock(IMOperation.class);
        ConvertCmd convertCmd = mock(ConvertCmd.class);

        List<ImageOperationDescription> parameters = Collections.singletonList(new ImageOperationDescription("gaussianBlur", "10"));

        when(imOperationFactory.create()).thenReturn(imOperation);

        try {
            defaultImageOperationsService.applyConversion(parameters, image, out);
        } catch (ImageOperationsException e) {
            verify(imOperationFactory).create();
            verify(imOperation).addImage(eq("-"));

            verifyNoMoreInteractions(imOperationFactory, convertCmdFactory, imOperation, convertCmd, image, out);

            throw e;
        }
    }

}
