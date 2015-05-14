package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.im4java.core.IM4JavaException;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // Needs imagemagick package installed
public class DefaultResizeImageServiceIntegrationTest {

    @Test
    public void resizeImageTest() throws IOException, InterruptedException, IM4JavaException {
        BufferedImage image = testImage(200, 200);

        assertThat(image.getWidth()).isEqualTo(200);
        assertThat(image.getHeight()).isEqualTo(200);
    }

    @Test
    public void resizeImageWidthTest() throws IOException, InterruptedException, IM4JavaException {
        BufferedImage image = testImage(200, null);

        assertThat(image.getWidth()).isEqualTo(200);
        assertThat(image.getHeight()).isNotEqualTo(200);
    }

    @Test
    public void resizeImageHeightTest() throws IOException, InterruptedException, IM4JavaException {
        BufferedImage image = testImage(null, 200);

        assertThat(image.getWidth()).isNotEqualTo(200);
        assertThat(image.getHeight()).isEqualTo(200);
    }

    @Test(expected = IM4JavaException.class)
    public void resizeImageFailTest() throws IOException, InterruptedException, IM4JavaException {
        testImage(null, null);
    }

    private BufferedImage testImage(Integer width, Integer height) throws IOException, FileNotFoundException, InterruptedException,
            IM4JavaException {
        DefaultResizeImageService defaultResizeImageService = new DefaultResizeImageService();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("codereview.jpg");

        File tempFile = File.createTempFile("testResize", ".jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        defaultResizeImageService.resizeImage(inputStream, width, height, outputStream);

        outputStream.close();

        return ImageIO.read(new FileInputStream(tempFile));
    }

}
