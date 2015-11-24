package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Blur implements ImageOperation {

    private final Pattern pattern = Pattern.compile("^\\((\\d+(?:\\.\\d+)?) *, *(\\d+(?:\\.\\d+)?)\\)$");
    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        double radius, sigma;

        try {
            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter blur: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            radius = Double.parseDouble(values.get(0));
            sigma = Double.parseDouble(values.get(1));


        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in blur: " + parameter, e);
        }

        if (radius < 0 || sigma < 0) {
            throw new ImageOperationsException("Parameters for blur must be greater or equal than 0: " + parameter);
        }

        return new IMOperation().blur(radius,sigma);
    }

    @Override
    public String getOperationName() {
        return "blur";
    }

}
