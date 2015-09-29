package io.corbel.oauth.repository.decorator;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.UserRepository;

/**
 * @author Francisco Sanchez
 */
public class LowerCaseDecoratorTest {

    public static final String ID = "ID";

    public static final String LOWER_CASE_USER = "lower_case_user";
    public static final String UPPER_CASE_USER = "LoWeR_CaSe_uSeR";
    public static final String LOWER_CASE_MAIL = "lower_case_user@server.com";
    public static final String UPPER_CASE_MAIL = "LoWeR_CaSe_uSeR@SerVer.Com";
    public static final String DOMAIN = "domain";

    public static final String PASSWORD = "passTest1";
    private LowerCaseDecorator lowerCaseDecorator;
    private UserRepository userRepositoryMock;
    private User userMock;

    @Before
    public void setup() {
        userRepositoryMock = mock(UserRepository.class);
        userMock = mock(User.class);
        lowerCaseDecorator = new LowerCaseDecorator(userRepositoryMock);
        when(userMock.getUsername()).thenReturn(UPPER_CASE_USER);
        when(userMock.getEmail()).thenReturn(UPPER_CASE_MAIL);
    }

    @Test
    public void saveTest() {
        lowerCaseDecorator.save(userMock);
        verify(userRepositoryMock, times(1)).save(userMock);
        verify(userMock, times(1)).setUsername(LOWER_CASE_USER);
        verify(userMock, times(1)).setEmail(LOWER_CASE_MAIL);
    }

    @Test
    public void findOneTest() {
        lowerCaseDecorator.findOne(LOWER_CASE_USER);
        verify(userRepositoryMock, times(1)).findOne(LOWER_CASE_USER);
    }

    @Test
    public void findOneUpperCaseTest() {
        lowerCaseDecorator.findOne(UPPER_CASE_USER);
        verify(userRepositoryMock, times(1)).findOne(LOWER_CASE_USER);
    }

    @Test
    public void findByUsernameTest() {
        lowerCaseDecorator.findByUsername(LOWER_CASE_USER);
        verify(userRepositoryMock, times(1)).findByUsername(LOWER_CASE_USER);
    }

    @Test
    public void findByUsernameUpperCaseTest() {
        lowerCaseDecorator.findByUsername(UPPER_CASE_USER);
        verify(userRepositoryMock, times(1)).findByUsername(LOWER_CASE_USER);
    }

    @Test
    public void findByUsernameAndDomainTest() {
        lowerCaseDecorator.findByUsernameAndDomain(LOWER_CASE_USER, DOMAIN);
        verify(userRepositoryMock, times(1)).findByUsernameAndDomain(LOWER_CASE_USER, DOMAIN);
    }

    @Test
    public void findByUsernameAndDomainUpperCaseTest() {
        lowerCaseDecorator.findByUsernameAndDomain(UPPER_CASE_USER, DOMAIN);
        verify(userRepositoryMock, times(1)).findByUsernameAndDomain(LOWER_CASE_USER, DOMAIN);
    }

    @Test
    public void findByEmailAndDomainTest() {
        lowerCaseDecorator.findByEmailAndDomain(LOWER_CASE_MAIL, DOMAIN);
        verify(userRepositoryMock, times(1)).findByEmailAndDomain(LOWER_CASE_MAIL, DOMAIN);
    }

    @Test
    public void findByEmailAndDomainUpperCaseTest() {
        lowerCaseDecorator.findByEmailAndDomain(UPPER_CASE_MAIL, DOMAIN);
        verify(userRepositoryMock, times(1)).findByEmailAndDomain(LOWER_CASE_MAIL, DOMAIN);
    }

    @Test
    public void existsTest() {
        lowerCaseDecorator.exists(LOWER_CASE_USER);
        verify(userRepositoryMock, times(1)).exists(LOWER_CASE_USER);
    }

    @Test
    public void existsUpperCaseTest() {
        lowerCaseDecorator.exists(UPPER_CASE_USER);
        verify(userRepositoryMock, times(1)).exists(LOWER_CASE_USER);
    }

    @Test
    public void deleteTest() {
        lowerCaseDecorator.delete(LOWER_CASE_USER);
        verify(userRepositoryMock, times(1)).delete(LOWER_CASE_USER);
    }

    @Test
    public void deleteUpperCaseTest() {
        lowerCaseDecorator.delete(UPPER_CASE_USER);
        verify(userRepositoryMock, times(1)).delete(LOWER_CASE_USER);
    }

    @Test
    public void deleteUserUpperCaseTest() {
        when(userMock.getUsername()).thenReturn(UPPER_CASE_USER);
        lowerCaseDecorator.delete(userMock);
        verify(userRepositoryMock, times(1)).delete(userMock);
        verify(userMock, times(1)).setUsername(LOWER_CASE_USER);
    }

    @Test
    public void patchUserUpperCaseTest() {
        lowerCaseDecorator.patch(ID, userMock, null);
        verify(userRepositoryMock, times(1)).patch(ID, userMock, null);
        verify(userMock, times(1)).setUsername(LOWER_CASE_USER);
        verify(userMock, times(1)).setEmail(LOWER_CASE_MAIL);

    }

    @Test
    public void patchUserUpperCaseTestOnlyUser() {
        lowerCaseDecorator.patch(userMock, null);
        verify(userRepositoryMock, times(1)).patch(userMock, null);
        verify(userMock, times(1)).setUsername(LOWER_CASE_USER);
        verify(userMock, times(1)).setEmail(LOWER_CASE_MAIL);

    }

}
