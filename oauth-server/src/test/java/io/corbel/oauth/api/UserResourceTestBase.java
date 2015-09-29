package io.corbel.oauth.api;

import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.BasicAuthProvider;
import io.corbel.lib.ws.auth.OAuthProvider;
import io.corbel.oauth.TestUtils;
import io.corbel.oauth.api.auth.ClientCredentialsAuthenticator;
import io.corbel.oauth.api.auth.TokenAuthenticator;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.Role;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.ClientRepository;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.UserService;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Before;
import org.junit.ClassRule;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Ricardo Martínez
 */
public abstract class UserResourceTestBase {

    protected final static String BEARER = "Bearer ";
    protected final static String TEST_GOOD_TOKEN = "xxxxxxxxxx";
    protected final static String TEST_ADMIN_TOKEN = "zzzzzzzzzzz";
    protected final static String TEST_BAD_TOKEN = "yyyyyyyyyy";
    protected final static String USERNAME_TEST = "usernameTest";

    protected final static String BASIC = "Basic ";
    protected final static String EMAIL = "email@email.com";
    protected static final String PASSWORD = "secret!";
    protected static final String TEST_DOMAIN_ID = "domain_id";
    protected static final String TEST_CLIENT_ID = "client_id";
    protected static final String TEST_CLIENT_SECRET = "client_secret";
    protected static final String TEST_CLIENT_DOMAIN = "domain";
    protected static final String TEST_BASIC_CLIENT_CRED = BASIC
            + Base64.encodeBase64URLSafeString((TEST_CLIENT_ID + ":" + TEST_CLIENT_SECRET).getBytes());
    protected static final String TEST_USER_ID = "test_user_id";
    protected static final String TEST_ADMIN_ID = "test_admin_id";
    protected static final String TEST_AVATAR_URI = "http://www.asjdflasjdfklasj.es/sdnajierniar.png";
    protected static final String AUTHORIZATION = "Authorization";

    protected static final UserService userServiceMock = mock(UserService.class);
    protected static final ClientService clientServiceMock = mock(ClientService.class);
    protected static final TokenParser tokenParserMock = mock(TokenParser.class);
    protected static final ClientRepository clientRepositoryMock = mock(ClientRepository.class);

    protected static Client TEST_CLIENT;
    protected static ClientCredentialsAuthenticator basicAuthenticatorMock = mock(ClientCredentialsAuthenticator.class);
    protected static TokenAuthenticator oauthAuthenticatorMock = mock(TokenAuthenticator.class);

    protected static BasicAuthFactory basicAuthFactory = new BasicAuthFactory<>(basicAuthenticatorMock, "", Client.class);
    protected static OAuthFactory oAuthFactory = new OAuthFactory<>(oauthAuthenticatorMock, "", TokenReader.class);
    protected static HttpServletRequest requestMock = mock(HttpServletRequest.class);


    public static class ContextInjectableProvider<T> extends AbstractBinder {
        protected final Class<T> clazz;
        protected final T instance;

        public ContextInjectableProvider(Class<T> clazz, T instance) {
            this.clazz = clazz;
            this.instance = instance;
        }

        @Override
        protected void configure() {
            bind(instance).to(clazz);
        }
    }

    @Before
    public void before() throws TokenVerificationException, AuthenticationException {

        reset(userServiceMock, tokenParserMock, clientRepositoryMock);

        when(tokenParserMock.parseAndVerify(TEST_BAD_TOKEN)).thenThrow(new TokenVerificationException("Invalid token"));


        BasicCredentials basic = new BasicCredentials(TEST_CLIENT_ID, TEST_CLIENT_SECRET);
        TEST_CLIENT = new Client();
        TEST_CLIENT.setName(TEST_CLIENT_ID);
        TEST_CLIENT.setKey(TEST_CLIENT_SECRET);
        TEST_CLIENT.setDomain(TEST_CLIENT_DOMAIN);
        when(basicAuthenticatorMock.authenticate(basic)).thenReturn(com.google.common.base.Optional.of(TEST_CLIENT));

        TokenReader tokenReaderMock = mock(TokenReader.class);
        TokenReader tokenAdminReaderMock = mock(TokenReader.class);

        when(tokenReaderMock.getToken()).thenReturn(TEST_GOOD_TOKEN);
        when(tokenParserMock.parseAndVerify(Mockito.eq(TEST_GOOD_TOKEN))).thenReturn(tokenReaderMock);

        when(tokenAdminReaderMock.getToken()).thenReturn(TEST_ADMIN_TOKEN);
        when(tokenParserMock.parseAndVerify(Mockito.eq(TEST_ADMIN_TOKEN))).thenReturn(tokenAdminReaderMock);

        TokenInfo tokenInfoMock = mock(TokenInfo.class);
        when(tokenReaderMock.getInfo()).thenReturn(tokenInfoMock);

        TokenInfo tokenAdminInfoMock = mock(TokenInfo.class);
        when(tokenAdminReaderMock.getInfo()).thenReturn(tokenAdminInfoMock);

        when(tokenInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(tokenInfoMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(tokenInfoMock.getTokenType()).thenReturn(TokenType.TOKEN);
        when(tokenInfoMock.getDomainId()).thenReturn(TEST_DOMAIN_ID);

        when(tokenAdminInfoMock.getUserId()).thenReturn(TEST_ADMIN_ID);
        when(tokenAdminInfoMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(tokenAdminInfoMock.getTokenType()).thenReturn(TokenType.TOKEN);

        when(userServiceMock.getUser(TEST_USER_ID)).thenReturn(TestUtils.createUserTest(Role.USER));
        when(userServiceMock.getUser(TEST_ADMIN_ID)).thenReturn(TestUtils.createUserTest(Role.ADMIN));

        when(oauthAuthenticatorMock.authenticate(TEST_GOOD_TOKEN)).thenReturn(com.google.common.base.Optional.of(tokenReaderMock));
        when(oauthAuthenticatorMock.authenticate(TEST_ADMIN_TOKEN)).thenReturn(com.google.common.base.Optional.of(tokenAdminReaderMock));
        when(oauthAuthenticatorMock.authenticate(TEST_BAD_TOKEN)).thenReturn(com.google.common.base.Optional.absent());
        
        when(clientRepositoryMock.findByName(TEST_CLIENT_ID)).thenReturn(TEST_CLIENT);

        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER + TEST_GOOD_TOKEN);
    }

    protected User createTestUser() {
        return createTestUser(new User());
    }

    protected User createTestUser(User user) {
        user.setId(TEST_USER_ID);
        user.setDomain(TEST_DOMAIN_ID);
        user.setEmail(EMAIL);
        user.setUsername(USERNAME_TEST);
        user.setPassword(PASSWORD);
        return user;
    }

}
