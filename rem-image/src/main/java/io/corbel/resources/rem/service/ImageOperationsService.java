package io.corbel.resources.rem.service;

import io.corbel.resources.rem.exception.ImageOperationsException;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.model.ImageOperationDescription;
import org.im4java.core.IM4JavaException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface ImageOperationsService {

    /**
     * Applies a set of operations to the InputStream and write the result on the OutputStream parameter. If there are target width and
     * height for a resize, the aspect ratio can change.
     *
     * @param parameters List of parameters passed from the url in the form [operationName, operationArgs].
     * @param image      The input image
     * @param out        The result will be write on this OutputStream (remember close it after use!)
     * @param format     (Optional) Image's output format (aka extension).
     * @param imMemoryLimit
     * @throws IOException          There's a problem with the streams provided
     * @throws InterruptedException System error with ImageMagic
     * @throws IM4JavaException     At least width or height
     */

    void applyConversion(List<ImageOperationDescription> parameters, InputStream image, OutputStream out, Optional<ImageFormat> format, String imMemoryLimit) throws ImageOperationsException,
            InterruptedException, IOException, IM4JavaException;
}
