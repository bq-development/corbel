package com.bq.oss.corbel.resources.rem.operation;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

public class ResizeWidth implements ImageOperation {

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        int width;

        try {
            width = Integer.parseInt(parameter);
        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image width: " + parameter, e);
        }

        if (width <= 0) {
            throw new ImageOperationsException("Width for resizeWidth must be greater than 0: " + parameter);
        }

        return new IMOperation().resize(width);
    }

    @Override
    public String getOperationName() {
        return "resizeWidth";
    }

}
