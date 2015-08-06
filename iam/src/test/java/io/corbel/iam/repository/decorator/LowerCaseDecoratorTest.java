package io.corbel.iam.repository.decorator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.iam.repository.UserRepository;
import io.corbel.lib.queries.ListQueryLiteral;
import io.corbel.lib.queries.LongQueryLiteral;
import io.corbel.lib.queries.QueryNodeImpl;
import io.corbel.lib.queries.StringQueryLiteral;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.request.QueryOperator;
import io.corbel.lib.queries.request.ResourceQuery;

@RunWith(MockitoJUnitRunner.class) public class LowerCaseDecoratorTest {

    @Mock private UserRepository repository;

    private LowerCaseDecorator lowerCaseDecorator;

    @Before
    public void before() {
        lowerCaseDecorator = new LowerCaseDecorator(repository);
    }

    @Test
    public void resourceQueryWithEmailTest() {
        ResourceQuery resourceQuery = new ResourceQueryBuilder().add("email", "TeSt", QueryOperator.$EQ).build();
        lowerCaseDecorator.count(resourceQuery);

        verify(repository).count(resourceQuery);

        assertThat((String) resourceQuery.iterator().next().getValue().getLiteral()).isEqualTo("test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resourceQueryWithBadTypeTest() {
        LongQueryLiteral literal = new LongQueryLiteral();
        literal.setLiteral(111l);
        QueryNodeImpl queryNode = new QueryNodeImpl(QueryOperator.$EQ, "email", literal);
        lowerCaseDecorator.count(new ResourceQuery(Arrays.asList(queryNode)));
    }

    @Test
    public void resourceQueryWithListTest() {
        StringQueryLiteral literalString = new StringQueryLiteral("TeSt");

        ListQueryLiteral literal = new ListQueryLiteral();
        literal.setLiteral(Arrays.asList(literalString));

        QueryNodeImpl queryNode = new QueryNodeImpl(QueryOperator.$IN, "email", literal);
        ResourceQuery resourceQuery = new ResourceQuery(Arrays.asList(queryNode));
        lowerCaseDecorator.count(resourceQuery);

        assertThat(((List<StringQueryLiteral>) resourceQuery.iterator().next().getValue().getLiteral()).get(0).getLiteral()).isEqualTo(
                "test");
    }

}
