package io.corbel.iam.service;

import io.corbel.iam.exception.UserProfileConfigurationException;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.User;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.CreateUserException;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    boolean existsByUsernameAndDomain(String username, String domainId);

    boolean existsByEmailAndDomain(String email, String domainId);

    void invalidateAllTokens(String userId);

    UserToken getSession(String token);

    User getUserProfile(User user, Set<String> userProfileFields) throws UserProfileConfigurationException;

    void sendMailResetPassword(String email, String clientId, String domain);

    List<User> findUserProfilesByDomain(Domain domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort)
            throws UserProfileConfigurationException;

    User findByDomainAndUsername(String domain, String username);

    User findByDomainAndEmail(String domain, String email);

    void addScopes(String userId, String... scopes);

    void removeScopes(String userId, String... scopes);

    long countUsersByDomain(String domainId, ResourceQuery query);
}
