package io.corbel.oauth.service;

import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.CreateUserException;

/**
 * @author Alberto J. Rubio
 */
public interface UserService {

    User getUser(String userId);

    User getUserByEmailAndDomain(String email, String domain);

    void deleteUser(String userId);

    boolean existsByUsernameAndDomain(String username, String domainId);

    User findByUserNameAndDomain(String username, String domain);

    void updateUser(User user, User userData, Client client) throws CreateUserException.DuplicatedUser;

    /**
     * Creates a new user
     * 
     * @param user the data to create the new user
     * @param client the client creating the user
     * @return the id of the new user
     * @throws CreateUserException.DuplicatedUser if there's already an user with the same username or the same email
     */
    String createUser(User user, Client client) throws CreateUserException.DuplicatedUser;

    void sendValidationEmail(User user, Client client);

    void sendMailResetPassword(String email, Client client);

    boolean confirmEmail(String email);
}
