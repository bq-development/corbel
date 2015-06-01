package com.bq.oss.corbel.resources.rem.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.im4java.core.IM4JavaException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.operation.*;
import com.google.common.collect.ImmutableMap;

@Ignore// Needs imagemagick package installed
public class DefaultImageOperationsServiceIntegrationTest {

    private static final String IMAGE_URL = "http://upload.wikimedia.org/wikipedia/commons/b/b6/SIPI_Jelly_Beans_4.1.07.tiff";

    private DefaultImageOperationsService imageOperationsService;

    @Before
    public void setUp() {
        imageOperationsService = new DefaultImageOperationsService(new DefaultImageOperationsService.IMOperationFactory(),
                new DefaultImageOperationsService.ConvertCmdFactory(), ImmutableMap.<String, ImageOperation>builder()
                        .put("resizeHeight", new ResizeHeight()).put("resize", new Resize()).put("cropFromCenter", new CropFromCenter())
                        .put("resizeAndFill", new ResizeAndFill()).build());
    }

    @Test
    public void resizeTest() throws IOException, ImageOperationsException, InterruptedException, IM4JavaException {
        try (InputStream image = new URL(IMAGE_URL).openStream(); FileOutputStream out = new FileOutputStream("/tmp/testImage.tiff")) {

            List<ImageOperationDescription> parameters = Arrays.asList(new ImageOperationDescription("resizeHeight", "200"),
                    new ImageOperationDescription("cropFromCenter", "(50, 50)"), new ImageOperationDescription("resize", "(100, 50)"),
                    new ImageOperationDescription("resizeAndFill", "(200, blue)"));

            imageOperationsService.applyConversion(parameters, image, out);
        }
    }

}
