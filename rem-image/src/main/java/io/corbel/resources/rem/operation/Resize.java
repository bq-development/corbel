package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Resize implements ImageOperation {

    private final Pattern pattern = Pattern.compile("^\\((\\d+) *, *(\\d+)\\)$");

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        int width, height;

        try {
            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter resize: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            width = Integer.parseInt(values.get(0));
            height = Integer.parseInt(values.get(1));


        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in resize: " + parameter, e);
        }

        if (width <= 0 || height <= 0) {
            throw new ImageOperationsException("Parameters for resize must be greater than 0: " + parameter);
        }

        return new IMOperation().resize(width, height, ">!");
    }

    @Override
    public String getOperationName() {
        return "resize";
    }
}
