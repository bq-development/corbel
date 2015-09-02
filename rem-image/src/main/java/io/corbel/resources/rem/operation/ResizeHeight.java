package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

public class ResizeHeight implements ImageOperation {

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {
        int height;

        try {
            height = Integer.parseInt(parameter);
        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad image height: " + parameter, e);
        }

        if (height <= 0) {
            throw new ImageOperationsException("Height for resizeHeight must be greater than 0: " + parameter);
        }

        return new IMOperation().resize(null, height, ">");
    }

    @Override
    public String getOperationName() {
        return "resizeHeight";
    }
}
