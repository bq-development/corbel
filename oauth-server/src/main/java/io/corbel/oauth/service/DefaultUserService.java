package io.corbel.oauth.service;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.CreateUserException;
import io.corbel.oauth.repository.UserRepository;

/**
 * @author Alberto J. Rubio
 */
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;

    private final MailValidationService mailValidationService;
    private final MailResetPasswordService mailResetPasswordService;
    private final MailChangePasswordService mailChangePasswordService;

    public DefaultUserService(UserRepository userRepository, MailValidationService mailValidationService,
            MailResetPasswordService mailResetPasswordService, MailChangePasswordService mailChangePasswordService) {
        this.userRepository = userRepository;
        this.mailValidationService = mailValidationService;
        this.mailResetPasswordService = mailResetPasswordService;
        this.mailChangePasswordService = mailChangePasswordService;
    }

    @Override
    public User getUser(String userId) {
        return userRepository.findOne(userId);
    }

    @Override
    public User getUserByEmailAndDomain(String email, String domain) {
        return userRepository.findByEmailAndDomain(email, domain);
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.delete(userId);
    }

    @Override
    public boolean existsByUsernameAndDomain(String username, String domainId) {
        return userRepository.existsByUsernameAndDomain(username, domainId);
    }

    @Override
    public User findByUserNameAndDomain(String username, String domain) {
        return userRepository.findByUsernameAndDomain(username, domain);
    }

    @Override
    public void updateUser(User user, User userData, Client client) throws CreateUserException.DuplicatedUser {
        try {
            boolean changePassword = userData.getPassword() != null && !Objects.equals(user.getPassword(), userData.getPassword());
            boolean changeEmail = userData.getEmail() != null && !Objects.equals(user.getEmail(), userData.getEmail());

            user.updateFields(userData);
            userRepository.save(user);

            if (changePassword) {
                notifyChangePassword(client, user);
            }
            if (changeEmail) {
                mailValidationService.sendMailValidation(client, user.getId(), userData.getEmail());
            }
        } catch (DataIntegrityViolationException e) {
            throw new CreateUserException.DuplicatedUser();
        }
    }

    @Override
    public String createUser(User user, Client client) throws CreateUserException.DuplicatedUser {
        user.setEmailValidated(false);
        user.setDomain(client.getDomain());
        try {
            user = userRepository.save(user);
            mailValidationService.sendMailValidation(client, user.getId(), user.getEmail());
            return user.getId();
        } catch (DataIntegrityViolationException e) {
            throw new CreateUserException.DuplicatedUser();
        }
    }

    @Override
    public void sendMailResetPassword(String email, Client client) {
        User user = getUserByEmailAndDomain(email, client.getDomain());
        if (user != null) {
            mailResetPasswordService.sendMailResetPassword(client, user.getId(), email);
        }
    }

    @Override
    public void sendValidationEmail(User user, Client client) {
        String currentEmail = user.getEmail();
        mailValidationService.sendMailValidation(client, user.getId(), currentEmail);
    }

    @Override
    public boolean confirmEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            User update = new User();
            update.setEmailValidated(true);
            return userRepository.patch(user.getId(), update);
        }
        return false;
    }

    private void notifyChangePassword(Client client, User user) {
        mailChangePasswordService.sendMailChangePassword(client, user.getUsername(), user.getEmail());
    }
}
