package com.bq.oss.corbel.resources.rem.operation;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

public class ResizeWidth implements ImageOperation {

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        try {

            return new IMOperation().resize(Integer.parseInt(parameter));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image width: " + parameter, e);
        }
    }

    @Override
    public String getOperationName() {
        return "resizeWidth";
    }

}
