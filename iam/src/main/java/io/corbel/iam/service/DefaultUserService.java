package io.corbel.iam.service;

import io.corbel.iam.exception.UserProfileConfigurationException;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.User;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.CreateUserException;
import io.corbel.iam.repository.UserRepository;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Sort;
import io.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.dao.DataIntegrityViolationException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Rubén Carrasco
 */
public class DefaultUserService implements UserService {

    private static final String DOMAIN = "domain";
    private final UserRepository userRepository;
    private final EventsService eventsService;
    private final UserTokenRepository userTokenRepository;
    private final AuthorizationRulesRepository authorizationRulesRepository;
    private final RefreshTokenService refreshTokenService;
    private final MailResetPasswordService mailResetPasswordService;
    private final Gson gson;

    public DefaultUserService(UserRepository userRepository, EventsService eventsService, UserTokenRepository userTokenRepository,
            AuthorizationRulesRepository authorizationRulesRepository, RefreshTokenService refreshTokenService,
            MailResetPasswordService mailResetPasswordService, Gson gson) {

        this.userRepository = userRepository;
        this.eventsService = eventsService;
        this.userTokenRepository = userTokenRepository;
        this.authorizationRulesRepository = authorizationRulesRepository;
        this.refreshTokenService = refreshTokenService;
        this.mailResetPasswordService = mailResetPasswordService;
        this.gson = gson;
    }

    @Override
    public boolean existsByUsernameAndDomain(String username, String domainId) {
        return userRepository.existsByUsernameAndDomain(username, domainId);
    }

    @Override
    public boolean existsByEmailAndDomain(String email, String domainId) {
        return userRepository.existsByEmailAndDomain(email, domainId);
    }

    @Override
    public String findUserDomain(String id) {
        return userRepository.findUserDomain(id);
    }

    @Override
    public List<User> findUsersByDomain(String domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort) {
        return userRepository.find(addUserDomainToQuery(domain, resourceQuery), pagination, sort);
    }

    private ResourceQuery addUserDomainToQuery(String domain, ResourceQuery resourceQuery) {
        ResourceQueryBuilder builder = new ResourceQueryBuilder(resourceQuery);
        builder.remove(DOMAIN).add(DOMAIN, domain);
        return builder.build();
    }

    @Override
    public User create(User user) throws CreateUserException {
        try {
            User createdUser = userRepository.save(user);
            eventsService.sendUserCreatedEvent(createdUser);
            return createdUser;
        } catch (DataIntegrityViolationException exception) {
            JsonElement error = gson.fromJson(exception.getCause().getMessage(), JsonElement.class).getAsJsonObject().get("err");

            if (error.getAsString().contains("email")) {
                throw new CreateUserException("email");
            } else {
                throw new CreateUserException("username");
            }
        }
    }

    @Override
    public User update(User user) {
        User updated = userRepository.save(user);
        eventsService.sendUserModifiedEvent(updated);
        return updated;
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
        signOut(user.getId());
        eventsService.sendUserDeletedEvent(user, user.getDomain());
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public void signOut(String userId, Optional<String> accessToken) {
        if (accessToken.isPresent()) {
            invalidateToken(accessToken.get());
            refreshTokenService.invalidateRefreshToken(userId, accessToken);
        } else {
            invalidateAllTokens(userId);
            refreshTokenService.invalidateRefreshToken(userId);
        }
    }

    @Override
    public void invalidateAllTokens(String userId) {
        List<UserToken> allUserTokens = userTokenRepository.findByUserId(userId);
        if (allUserTokens != null) {
            allUserTokens.stream().forEach(token -> invalidateToken(token.getToken()));
        }
    }

    @Override
    public UserToken getSession(String token) {
        return  userTokenRepository.findByToken(token);
    }

    private void invalidateToken(String accessToken) {
        authorizationRulesRepository.deleteByToken(accessToken);
        userTokenRepository.delete(accessToken);
    }

    @Override
    public User getUserProfile(User user, Set<String> userProfileFields) throws UserProfileConfigurationException {
        if (userProfileFields != null) {
            User profile = new User();
            for (String userProfileField : userProfileFields) {
                try {
                    Object value = PropertyUtils.getProperty(user, userProfileField);
                    PropertyUtils.setProperty(profile, userProfileField, value);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new UserProfileConfigurationException("Invalid user profile configuration for domain " + user.getDomain(), e);
                }

            }
            return profile;
        }
        return null;
    }

    @Override
    public void sendMailResetPassword(String email, String clientId, String domain) {
        Optional.ofNullable(userRepository.findByDomainAndEmail(domain, email)).ifPresent(
                user -> mailResetPasswordService.sendMailResetPassword(clientId, user.getId(), email, domain));
    }

    @Override
    public List<User> findUserProfilesByDomain(Domain domain, ResourceQuery resourceQuery, Pagination pagination, Sort sort)
            throws UserProfileConfigurationException {
        List<User> users = userRepository.find(addUserDomainToQuery(domain.getId(), resourceQuery), pagination, sort);

        List<User> userProfiles = new ArrayList<>();
        Set<String> userProfileFields = domain.getUserProfileFields();
        for (User user : users) {
            userProfiles.add(getUserProfile(user, userProfileFields));
        }

        return userProfiles;
    }

    @Override
    public User findByDomainAndUsername(String domain, String username) {
        return userRepository.findByUsernameAndDomain(username, domain);
    }

    @Override
    public User findByDomainAndEmail(String domain, String email) {
        return userRepository.findByDomainAndEmail(domain, email);
    }

    @Override
    public void addScopes(String userId, String... scopes) {
        userRepository.addScopes(userId, scopes);
    }

    @Override
    public void removeScopes(String userId, String... scopes) {
        userRepository.removeScopes(userId, scopes);
    }

    @Override
    public long countUsersByDomain(String domain, ResourceQuery resourceQuery) {
        return userRepository.count(addUserDomainToQuery(domain, resourceQuery));
    }

}
