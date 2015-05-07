package com.bq.oss.corbel.iam.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bq.oss.corbel.iam.exception.UserProfileConfigurationException;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.CreateUserException;
import com.bq.oss.lib.queries.request.AggregationResult;
import com.bq.oss.lib.queries.request.Pagination;
import com.bq.oss.lib.queries.request.ResourceQuery;
import com.bq.oss.lib.queries.request.Sort;

/**
 * @author Rubén Carrasco
 * 
 */
public interface UserService {

    String findUserDomain(String id);

    User update(User user);

    User create(User user) throws CreateUserException;

    User findById(String id);

    List<User> findUsersByDomain(String domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort);

    void signOut(String userId, Optional<String> accessToken);

    default void signOut(String userId) {
        signOut(userId, Optional.empty());
    }

    void delete(User user);

    boolean existsByUsernameAndDomain(String username, String domain);

    User getUserProfile(User user, Set<String> userProfileFields) throws UserProfileConfigurationException;

    void sendMailResetPassword(String email, String clientId, String domain);

    List<User> findUserProfilesByDomain(Domain domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort)
            throws UserProfileConfigurationException;

    User findByDomainAndUsername(String domain, String username);

    User findByDomainAndEmail(String domain, String email);

    void addScopes(String userId, String... scopes);

    void removeScopes(String userId, String... scopes);

    AggregationResult countUsersByDomain(String domainId, ResourceQuery query);
}
