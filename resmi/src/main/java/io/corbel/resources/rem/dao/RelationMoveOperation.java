package io.corbel.resources.rem.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelationMoveOperation {

    private static final Pattern OPERATION_PATTERN = Pattern.compile("\\$pos\\((\\d+)\\)");

    private final long value;

    public static RelationMoveOperation create(String moveOperation) {
        Matcher matcher = OPERATION_PATTERN.matcher(moveOperation);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The operation " + moveOperation + " is not valid");
        }
        int value = Integer.parseInt(matcher.group(1));
        return new RelationMoveOperation(value);
    }

    public RelationMoveOperation(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
