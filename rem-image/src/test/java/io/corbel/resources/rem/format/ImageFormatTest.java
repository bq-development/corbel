package io.corbel.resources.rem.format;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


public class ImageFormatTest {

    @Test(expected = IllegalArgumentException.class)
    public void nonExistingFormatTest() {
        ImageFormat imageFormat = ImageFormat.safeValueOf("NOT_A_REAL_FORMAT");
    }

    @Test
    public void existingFormatTest() throws ImageOperationsException {
        final String PNG = "PNG";
        ImageFormat imageFormat = ImageFormat.safeValueOf(PNG);
        assertThat(imageFormat.toString()).isEqualTo(PNG);

        final String JPG = "JPG";
        imageFormat = ImageFormat.safeValueOf(JPG);
        assertThat(imageFormat.toString()).isEqualTo(JPG);
    }

    @Test
    public void existingFormatTestLowerCase() throws ImageOperationsException {
        final String png = "png";
        ImageFormat imageFormat = ImageFormat.safeValueOf(png);
        assertThat(imageFormat.toString()).isEqualTo(png.toUpperCase());

        final String jpg = "jpg";
        imageFormat = ImageFormat.safeValueOf(jpg);
        assertThat(imageFormat.toString()).isEqualTo(jpg.toUpperCase());
    }
}
