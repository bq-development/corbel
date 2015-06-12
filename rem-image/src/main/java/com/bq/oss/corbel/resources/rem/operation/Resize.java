package com.bq.oss.corbel.resources.rem.operation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;

public class Resize implements ImageOperation {

    private static final Pattern pattern = Pattern.compile("^\\((\\d+) *, *(\\d+)\\)$");

    @Override
    public IMOps apply(String parameter) throws ImageOperationsException {

        try {
            Matcher matcher = pattern.matcher(parameter);

            if (!matcher.matches()) {
                throw new ImageOperationsException("Bad parameter resize: " + parameter);
            }

            List<String> values = getValues(parameter, matcher);

            return new IMOperation().resize(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), '!');

        } catch (NumberFormatException e) {
            throw new ImageOperationsException("Bad dimension parameter in resize: " + parameter, e);
        }

    }

    @Override
    public String getOperationName() {
        return "resize";
    }

}
