package com.bq.oss.corbel.iam.repository.decorator;

import java.util.List;

import org.bouncycastle.util.Strings;

import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.lib.queries.StringQueryLiteral;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

/**
 * @author Francisco Sanchez
 */
public class LowerCaseDecorator extends UserRepositoryDecorator {

    public LowerCaseDecorator(UserRepository decoratedUserRepository) {
        super(decoratedUserRepository);
    }

    @Override
    public User findByDomainAndEmail(String domain, String email) {
        return decoratedUserRepository.findByDomainAndEmail(domain, Strings.toLowerCase(email));
    }

    @Override
    public List<User> find(ResourceQuery resourceQuery, Pagination pagination, Sort sort) {
        emailQueryToLowerCase(resourceQuery);
        return decoratedUserRepository.find(resourceQuery, pagination, sort);
    }

    @Override
    public AggregationResult count(ResourceQuery resourceQuery) {
        emailQueryToLowerCase(resourceQuery);
        return decoratedUserRepository.count(resourceQuery);
    }

    @Override
    public User save(User user) {
        emailUserToLowerCase(user);
        return decoratedUserRepository.save(user);
    }

    @Override
    public <S extends User> List<S> save(Iterable<S> users) {
        users.forEach(user -> emailUserToLowerCase(user));
        return decoratedUserRepository.save(users);
    }

    private void emailUserToLowerCase(User user) {
        user.setEmail(Strings.toLowerCase(user.getEmail()));
    }

    private void emailQueryToLowerCase(ResourceQuery resourceQuery) {
        resourceQuery.forEach(queryNode -> {
            if ("email".equals(queryNode.getField())) {
                StringQueryLiteral stringQueryLiteral = (StringQueryLiteral) queryNode.getValue().getLiteral();
                stringQueryLiteral.setLiteral(Strings.toLowerCase(stringQueryLiteral.getLiteral()));
            }
        });
    }

}
