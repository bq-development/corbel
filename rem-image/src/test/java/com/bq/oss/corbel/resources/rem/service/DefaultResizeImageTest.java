package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.*;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class DefaultResizeImageTest {

    @Mock private ConvertCmd convertCmd;
    @Spy private final DefaultResizeImageService defaultResizeImageService = new DefaultResizeImageService();

    @Test
    public void resizeImageTest() throws IOException, InterruptedException, IM4JavaException {
        IMOperation argument = testImage(200, 200);
        assertThat(argument.toString()).isEqualTo("- -resize 200x200! - ");
    }

    @Test
    public void resizeImageWidthTest() throws IOException, InterruptedException, IM4JavaException {
        IMOperation argument = testImage(200, null);
        assertThat(argument.toString()).isEqualTo("- -resize 200x - ");
    }

    @Test
    public void resizeImageHeightTest() throws IOException, InterruptedException, IM4JavaException {
        IMOperation argument = testImage(null, 200);
        assertThat(argument.toString()).isEqualTo("- -resize x200 - ");
    }

    @Test(expected = IM4JavaException.class)
    public void resizeImageFailTest() throws IOException, InterruptedException, IM4JavaException {
        doThrow(IM4JavaException.class).when(convertCmd).run(any());
        testImage(null, null);
    }

    private IMOperation testImage(Integer width, Integer height) throws IOException, FileNotFoundException, InterruptedException,
            IM4JavaException {
        doReturn(convertCmd).when(defaultResizeImageService).createConvertCmd();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("codereview.jpg");

        File tempFile = File.createTempFile("testResize", ".jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        defaultResizeImageService.resizeImage(inputStream, width, height, outputStream);

        outputStream.close();

        ArgumentCaptor<IMOperation> argument = ArgumentCaptor.forClass(IMOperation.class);
        verify(convertCmd).run(argument.capture());
        return argument.getValue();
    }
}
