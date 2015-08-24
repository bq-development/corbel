package io.corbel.resources.rem.dao;

import java.util.Date;
import java.util.function.Function;

import io.corbel.lib.queries.DateQueryLiteral;
import io.corbel.lib.queries.LongQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.request.QueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;

/**
 * @author Rubén Carrasco
 *
 */
public class DateQueryNodeTransformer implements Function<QueryNode, QueryNode> {

    private enum VALID_OPERATORS { $EQ, $GT, $GTE, $LT, $LTE, $NE }

    @Override
    public QueryNode apply(QueryNode t) {
        return new QueryNodeImpl(t.getOperator(), t.getField(), transformDateValue(t.getField(), t.getOperator(), t.getValue()));
    }

    private QueryLiteral<?> transformDateValue(String field, QueryOperator operator, QueryLiteral<?> value) {
        if (field.equals(ReservedFields._UPDATED_AT) || field.equals(ReservedFields._CREATED_AT)) {
            if (VALID_OPERATORS.valueOf(operator.toString()) != null) {
                try {
                    return new DateQueryLiteral(new Date(((LongQueryLiteral) value).getLiteral()));
                } catch (ClassCastException ignored) {}
            }
        }
        return value;
    }
}
