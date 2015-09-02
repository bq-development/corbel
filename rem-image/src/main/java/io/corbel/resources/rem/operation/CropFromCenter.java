package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CropFromCenter implements ImageOperation {

    private final Pattern pattern = Pattern.compile("\\((\\d+) *, *(\\d+)\\)");

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        int xratio, yratio;

        try {

            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter cropFromCenter: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            xratio = Integer.parseInt(values.get(0));
            yratio = Integer.parseInt(values.get(1));

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in crop from center operation: " + parameter, e);
        }

        if (xratio <= 0 || yratio <= 0) {
            throw new ImageOperationsException("Parameters for cropFromCenter must be greater than 0: " + parameter);
        }

        IMOperation subOperation = new IMOperation();
        subOperation.gravity("center").crop(xratio, yratio, 0, 0);
        return subOperation;

    }

    @Override
    public String getOperationName() {
        return "cropFromCenter";
    }
}
