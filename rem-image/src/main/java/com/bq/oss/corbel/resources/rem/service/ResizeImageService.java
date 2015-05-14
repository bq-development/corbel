package com.bq.oss.corbel.resources.rem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.im4java.core.IM4JavaException;

public interface ResizeImageService {

    /**
     * Resize the image of the InputStream and write the result on the OutputStream parameter. If there are target width and height, the
     * aspect ratio can change. If there is a null width or height target (not both!) the aspect ratio will be maintained.
     * 
     * @param image The input image
     * @param width The target width
     * @param height The target height
     * @param out The result will be write on this OutputStream (remember close it after use!)
     * @throws IOException There's a problem with the streams provided
     * @throws InterruptedException System error with ImageMagic
     * @throws IM4JavaException At least width or height
     */
    public void resizeImage(InputStream image, Integer width, Integer height, OutputStream out) throws IOException, InterruptedException,
            IM4JavaException;
}
