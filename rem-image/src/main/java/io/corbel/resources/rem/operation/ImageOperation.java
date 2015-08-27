package io.corbel.resources.rem.operation;

import io.corbel.resources.rem.exception.ImageOperationsException;
import org.im4java.core.IMOps;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public interface ImageOperation {

    IMOps apply(String parameter) throws ImageOperationsException;

    String getOperationName();

    default List<String> getValues(String param, Matcher matcher) {
        int groupCount = matcher.groupCount() + 1;
        List<String> valuesToReturn = new ArrayList<>(groupCount);

        for (int i = 1; i < groupCount; ++i) {
            valuesToReturn.add(param.substring(matcher.start(i), matcher.end(i)));
        }
        return valuesToReturn;
    }
}
