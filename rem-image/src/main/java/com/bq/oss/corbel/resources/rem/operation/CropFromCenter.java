package com.bq.oss.corbel.resources.rem.operation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

public class CropFromCenter implements ImageOperation {

    private static final Pattern pattern = Pattern.compile("\\((\\d+) *, *(\\d+)\\)");

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

        IMOperation subOperation = new IMOperation();
        subOperation.gravity("center");
        subOperation.crop(xratio, yratio, -xratio / 2, -yratio / 2);

        return subOperation;

    }

    @Override
    public String getOperationName() {
        return "cropFromCenter";
    }

}
