package com.bq.oss.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bq.oss.corbel.iam.exception.UserProfileConfigurationException;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.model.UserToken;
import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.corbel.iam.repository.UserTokenRepository;
import io.corbel.lib.queries.builder.ResourceQueryBuilder;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;
import com.google.gson.Gson;

/**
 * @author Alexander De Leon
 *
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultUserServiceTest {

    private static final String TEST_TOKEN = "Token";
    private static final String TEST_USER = "user";
    private static final String TEST_TOKEN_2 = "Token2";
    private static final String TEST_DOMAIN = "domain";
    private static final String TEST_USER_EMAIL = "email";
    private static final String TEST_USER_FIRST_NAME = "firstname";
    private static final String TEST_USER_LAST_NAME = "lastname";
    private static final String USER_ID = "userId";
    private static final String CLIENT_ID = "asnroejasdklf";
    private static final String DOMAIN_ID = "domain_id";
    private static final String[] TEST_SCOPES = {"kasdjflksaj", "jsdkafj", "haweuriwu"};

    @Mock private DefaultUserService service;
    @Mock private UserRepository userRepositoryMock;
    @Mock private EventsService eventServiceMock;
    @Mock private UserTokenRepository userTokenRepositoryMock;
    @Mock private AuthorizationRulesRepository authorizationRulesRepositoryMock;
    @Mock private RefreshTokenService refreshTokenServiceMock;

    @Mock private MailResetPasswordService mailResetPasswordServiceMock;

    @Before
    public void setup() {
        service = new DefaultUserService(userRepositoryMock, eventServiceMock, userTokenRepositoryMock, authorizationRulesRepositoryMock,
                refreshTokenServiceMock, mailResetPasswordServiceMock, new Gson());
    }

    @Test
    public void testSignoutAll() {
        UserToken token = new UserToken();
        token.setToken(TEST_TOKEN);
        when(userTokenRepositoryMock.findByUserId(TEST_USER)).thenReturn(Arrays.asList(token));
        service.signOut(TEST_USER);
        verify(refreshTokenServiceMock).invalidateRefreshToken(TEST_USER);
        verify(authorizationRulesRepositoryMock).deleteByToken(TEST_TOKEN);
        verify(userTokenRepositoryMock).delete(TEST_TOKEN);

    }

    @Test
    public void testSignoutAllNoTokens() {
        service.signOut(TEST_USER);
        verify(refreshTokenServiceMock).invalidateRefreshToken(TEST_USER);
        verify(userTokenRepositoryMock).findByUserId(TEST_USER);
        verifyNoMoreInteractions(authorizationRulesRepositoryMock, userTokenRepositoryMock);

    }

    @Test
    public void testSignout() {
        UserToken token1 = new UserToken();
        token1.setToken(TEST_TOKEN);
        UserToken token2 = new UserToken();
        token2.setToken(TEST_TOKEN_2);
        when(userTokenRepositoryMock.findByUserId(TEST_USER)).thenReturn(Arrays.asList(token1, token2));
        service.signOut(TEST_USER, Optional.of(TEST_TOKEN));
        verify(refreshTokenServiceMock).invalidateRefreshToken(TEST_USER, Optional.of(TEST_TOKEN));
        verify(authorizationRulesRepositoryMock).deleteByToken(TEST_TOKEN);
        verify(userTokenRepositoryMock).delete(TEST_TOKEN);
        verify(refreshTokenServiceMock, never()).invalidateRefreshToken(TEST_USER, Optional.of(TEST_TOKEN_2));
        verify(authorizationRulesRepositoryMock, never()).deleteByToken(TEST_TOKEN_2);
        verify(userTokenRepositoryMock, never()).delete(TEST_TOKEN_2);
    }

    @Test
    public void testGetUserProfile() throws UserProfileConfigurationException {
        User user = new User();
        user.setDomain(TEST_DOMAIN);
        user.setEmail(TEST_USER_EMAIL);
        user.setFirstName(TEST_USER_FIRST_NAME);
        user.setLastName(TEST_USER_LAST_NAME);

        Set<String> fields = new HashSet<>(Arrays.asList("email", "firstName", "lastName"));
        when(userRepositoryMock.findById(USER_ID)).thenReturn(user);

        User userProfile = service.getUserProfile(user, fields);

        assertThat(userProfile).isNotEqualTo(user);
        assertThat(userProfile.getEmail()).isEqualTo(user.getEmail());
        assertThat(userProfile.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userProfile.getLastName()).isEqualTo(user.getLastName());
        assertThat(userProfile.getDomain()).isNull();
    }

    @Test(expected = UserProfileConfigurationException.class)
    public void testGetUserProfileException() throws UserProfileConfigurationException {
        User user = new User();
        user.setDomain(TEST_DOMAIN);

        Set<String> fields = new HashSet<>(Arrays.asList("asdf"));
        when(userRepositoryMock.findById(USER_ID)).thenReturn(user);

        service.getUserProfile(user, fields);
    }

    @Test
    public void testSendMailResetPassword() {
        User testUser = new User();
        testUser.setId(TEST_USER);

        when(userRepositoryMock.findByDomainAndEmail(TEST_DOMAIN, TEST_USER_EMAIL)).thenReturn(testUser);

        service.sendMailResetPassword(TEST_USER_EMAIL, CLIENT_ID, TEST_DOMAIN);

        verify(mailResetPasswordServiceMock).sendMailResetPassword(CLIENT_ID, TEST_USER, TEST_USER_EMAIL, TEST_DOMAIN);
        verifyNoMoreInteractions(mailResetPasswordServiceMock);
    }

    @Test
    public void testSendMailResetPasswordWithInexistentUser() {
        when(userRepositoryMock.findByDomainAndEmail(TEST_DOMAIN, TEST_USER_EMAIL)).thenReturn(null);

        service.sendMailResetPassword(TEST_USER_EMAIL, CLIENT_ID, TEST_DOMAIN);

        verify(mailResetPasswordServiceMock, never()).sendMailResetPassword(any(), any(), any(), eq(DOMAIN_ID));
    }

    @Test
    public void testFindByDomainAndUsername() {
        service.findByDomainAndUsername(TEST_DOMAIN, TEST_USER);
        verify(userRepositoryMock).findByUsernameAndDomain(TEST_USER, TEST_DOMAIN);
    }

    @Test
    public void testFindByDomainAndEmail() {
        service.findByDomainAndEmail(TEST_DOMAIN, TEST_USER_EMAIL);
        verify(userRepositoryMock).findByDomainAndEmail(TEST_DOMAIN, TEST_USER_EMAIL);
    }

    @Test
    public void testAddScopes() {
        service.addScopes(USER_ID, TEST_SCOPES);
        verify(userRepositoryMock).addScopes(USER_ID, TEST_SCOPES);
    }

    @Test
    public void testRemoveScopes() {
        service.removeScopes(USER_ID, TEST_SCOPES);
        verify(userRepositoryMock).removeScopes(USER_ID, TEST_SCOPES);
    }

    @Test
    public void testCountUsersByDomain() {
        ResourceQueryBuilder builder = new ResourceQueryBuilder();
        builder.add("domain", TEST_DOMAIN);
        ResourceQuery builded = builder.build();

        service.countUsersByDomain(TEST_DOMAIN, null);
        verify(userRepositoryMock, only()).count(eq(builded));
    }
}
