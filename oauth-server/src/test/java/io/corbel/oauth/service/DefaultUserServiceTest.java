package io.corbel.oauth.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.corbel.oauth.TestUtils;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.Role;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.CreateUserException.DuplicatedUser;
import io.corbel.oauth.repository.UserRepository;

/**
 * @author Cristian del Cerro
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultUserServiceTest {

    private static final String USER_PROFILE_TEST = "userProfileTest";
    private static final String BAD_USER_TEST = "badUserTest";
    private static final String USER_TEST = "userTest";
    private static Client TEST_CLIENT;
    private static final String TEST_DOMAIN = "TEST_DOMAIN";

    @Mock private UserRepository userRepositoryMock;
    @Mock private MailValidationService mailValidationServiceMock;
    @Mock private MailResetPasswordService mailResetPasswordServiceMock;
    @Mock private MailChangePasswordService mailChangePasswordService;

    private DefaultUserService userService;

    @Before
    public void setup() {
        userService = new DefaultUserService(userRepositoryMock, mailValidationServiceMock, mailResetPasswordServiceMock,
                mailChangePasswordService);

        TEST_CLIENT = new Client();
        TEST_CLIENT.setDomain(TEST_DOMAIN);
    }

    @Test
    public void testGetUser() {
        User userTest = TestUtils.createUserTest(Role.USER);

        when(userRepositoryMock.findOne(USER_TEST)).thenReturn(userTest);
        assertThat(userService.getUser(USER_TEST)).isEqualTo(userTest);

    }

    @Test
    public void testVerifierUser() {
        User userTest = TestUtils.createUserTest(Role.USER);
        when(userRepositoryMock.findByUsernameAndDomain(USER_TEST, TEST_DOMAIN)).thenReturn(userTest);

        assertThat(userService.findByUserNameAndDomain(USER_TEST, TEST_DOMAIN)).isNotNull();
        assertThat(userService.findByUserNameAndDomain(BAD_USER_TEST, TEST_DOMAIN)).isNull();
    }

    @Test
    public void testGetUserProfile() {
        User user = TestUtils.createUserTest(Role.USER);
        when(userRepositoryMock.findOne(USER_PROFILE_TEST)).thenReturn(user);

        assertThat(userService.getUser(USER_PROFILE_TEST)).isEqualTo(user);
    }

    @Test
    public void testCreateUser() throws DuplicatedUser {
        final String testId = "1234";
        final User user = TestUtils.createUserTest(Role.USER);
        when(userRepositoryMock.save(user)).thenAnswer(invocation -> {
            user.setId(testId);
            return user;
        });
        String id = userService.createUser(user, TEST_CLIENT);
        verify(userRepositoryMock, times(1)).save(user);
        assertThat(id).isEqualTo(testId);
    }

    @Test
    public void testDeleteUser() {
        userService.deleteUser(USER_TEST);
        verify(userRepositoryMock, times(1)).delete(USER_TEST);
    }

    @Test
    public void testExistsByUsernameAndDomain() {
        userService.existsByUsernameAndDomain(USER_TEST, TEST_DOMAIN);
        verify(userRepositoryMock).existsByUsernameAndDomain(USER_TEST, TEST_DOMAIN);
    }

    @Test
    public void testFindByUserNameAndDomain() {
        userService.findByUserNameAndDomain(USER_TEST, TEST_DOMAIN);
        verify(userRepositoryMock).findByUsernameAndDomain(USER_TEST, TEST_DOMAIN);
    }

    @Test
    public void testSendMailResetPassword() {
        String email = "email";
        User user = TestUtils.createUserTest(Role.USER);
        when(userRepositoryMock.findByEmailAndDomain(email, TEST_DOMAIN)).thenReturn(user);

        userService.sendMailResetPassword(email, TEST_CLIENT);

        verify(mailResetPasswordServiceMock).sendMailResetPassword(TEST_CLIENT, user.getId(), email);
    }

    @Test
    public void testSendMailResetPasswordWhenUserNotExists() {
        String email = "email";

        userService.sendMailResetPassword(email, TEST_CLIENT);

        verify(mailResetPasswordServiceMock, times(0)).sendMailResetPassword(any(), any(), any());
    }

    @Test
    public void testSendMailWhenChangePassword() throws DuplicatedUser {
        User user = TestUtils.createUserTest(Role.USER);

        User userData = TestUtils.createUserTest(Role.USER);
        userData.setPassword("anyPass");
        userData.setEmail(null);

        when(userRepositoryMock.findById(user.getId())).thenReturn(user);
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);

        userService.updateUser(user, userData, TEST_CLIENT);

        verify(mailChangePasswordService).sendMailChangePassword(TEST_CLIENT, user.getUsername(), user.getEmail());
    }

    @Test
    public void testSendMailWhenChangeEmail() throws DuplicatedUser {
        User user = TestUtils.createUserTest(Role.USER);

        User userData = TestUtils.createUserTest(Role.USER);
        userData.setPassword(null);
        userData.setEmail("emailToUpdate");

        when(userRepositoryMock.findById(user.getId())).thenReturn(user);

        userService.updateUser(user, userData, TEST_CLIENT);

        verify(mailValidationServiceMock).sendMailValidation(TEST_CLIENT, user.getId(), user.getEmail());
    }
}