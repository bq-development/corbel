package io.corbel.resources.rem.service;

import com.google.common.collect.ImmutableMap;
import io.corbel.resources.rem.exception.ImageOperationsException;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.model.ImageOperationDescription;
import io.corbel.resources.rem.operation.*;
import org.im4java.core.IM4JavaException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Ignore// Needs imagemagick package installed
public class DefaultImageOperationsServiceIntegrationTest {

    private static String IM_MEMORY_LIMIT = "200MiB";
    private static final String IMAGE_PATH = "src/test/resources/logo.png";
    private static final String IMAGE_URL = "http://upload.wikimedia.org/wikipedia/commons/b/b6/SIPI_Jelly_Beans_4.1.07.tiff";
    private Optional<ImageFormat> imageFormatNull = Optional.empty();

    private DefaultImageOperationsService imageOperationsService;

    @Before
    public void setUp() throws ImageOperationsException {
        imageOperationsService = new DefaultImageOperationsService(new DefaultImageOperationsService.IMOperationFactory(),
                new DefaultImageOperationsService.ConvertCmdFactory(), ImmutableMap.<String, ImageOperation>builder()
                .put("resizeWidth", new ResizeWidth()).put("resizeHeight", new ResizeHeight()).put("resize", new Resize())
                .put("crop", new Crop()).put("cropFromCenter", new CropFromCenter()).put("resizeAndFill", new ResizeAndFill())
                .build());
    }

    @Test
    public void test() throws IOException, ImageOperationsException, InterruptedException, IM4JavaException {
        try (InputStream image = new URL(IMAGE_URL).openStream(); FileOutputStream out = new FileOutputStream("/tmp/testImage.tiff")) {
            List<ImageOperationDescription> parameters = Collections.singletonList(new ImageOperationDescription("crop", "(0, 0, 2, 2)"));
            imageOperationsService.applyConversion(parameters, image, out, imageFormatNull, IM_MEMORY_LIMIT);
        }
    }

    @Test
    public void resizeTest() throws IOException, ImageOperationsException, InterruptedException, IM4JavaException {
        try (FileInputStream in = new FileInputStream(IMAGE_PATH); FileOutputStream out = new FileOutputStream("/tmp/testImage.png")) {

            List<ImageOperationDescription> parameters = Arrays.asList(new ImageOperationDescription("resizeHeight", "200"),
                    new ImageOperationDescription("cropFromCenter", "(50, 50)"),
                    new ImageOperationDescription("extension", "PNG"),
                    new ImageOperationDescription("resize", "(100, 50)"), new ImageOperationDescription("resizeAndFill", "(200, blue)")
            );
            imageOperationsService.applyConversion(parameters, in, out, imageFormatNull, IM_MEMORY_LIMIT);
        }
    }
}
