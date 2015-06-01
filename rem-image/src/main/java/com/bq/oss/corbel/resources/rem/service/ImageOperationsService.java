package com.bq.oss.corbel.resources.rem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.im4java.core.IM4JavaException;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;

public interface ImageOperationsService {

    /**
     * Applies a set of operations to the InputStream and write the result on the OutputStream parameter. If there are target width and
     * height for a resize, the aspect ratio can change.
     * 
     * @param parameters List of parameters passed from the url in the form [operationName, operationArgs].
     * @param image The input image
     * @param out The result will be write on this OutputStream (remember close it after use!)
     * @throws IOException There's a problem with the streams provided
     * @throws InterruptedException System error with ImageMagic
     * @throws IM4JavaException At least width or height
     */

    void applyConversion(List<ImageOperationDescription> parameters, InputStream image, OutputStream out) throws ImageOperationsException,
            InterruptedException, IOException, IM4JavaException;
}
