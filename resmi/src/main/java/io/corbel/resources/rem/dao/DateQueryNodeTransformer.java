package io.corbel.resources.rem.dao;

import io.corbel.lib.queries.DateQueryLiteral;
import io.corbel.lib.queries.LongQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.request.QueryLiteral;
import io.corbel.lib.queries.request.QueryNode;
import io.corbel.lib.queries.request.QueryOperator;

import java.util.Date;
import java.util.function.Function;

/**
 * @author Rubén Carrasco
 *
 */
public class DateQueryNodeTransformer implements Function<QueryNode, QueryNode> {

    @Override
    public QueryNode apply(QueryNode t) {
        return new QueryNodeImpl(t.getOperator(), t.getField(), tranformDateValue(t.getField(), t.getOperator(), t.getValue()));
    }

    @SuppressWarnings("incomplete-switch")
    private QueryLiteral<?> tranformDateValue(String field, QueryOperator operator, QueryLiteral<?> value) {
        if (field.equals(ReservedFields._UPDATED_AT) || field.equals(ReservedFields._CREATED_AT)) {
            switch (operator) {
                case $EQ:
                case $GT:
                case $GTE:
                case $LT:
                case $LTE:
                case $NE:
                    try {
                        return new DateQueryLiteral(new Date(((LongQueryLiteral) value).getLiteral()));
                    } catch (ClassCastException e) {}
            }
        }
        return value;
    }
}
