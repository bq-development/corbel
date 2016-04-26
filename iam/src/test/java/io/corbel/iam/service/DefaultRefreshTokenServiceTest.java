package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.UserRepository;
import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.token.repository.OneTimeAccessTokenRepository;

/**
 * @author Alberto J. Rubio
 */
public class DefaultRefreshTokenServiceTest {

    private static final long REFRESH_TOKEN_DURATION_IN_SECONDS = Long.parseLong("31536000");

    private static final String TEST_CLIENT = "test_client";
    private static final String TEST_USER = "test_user";
    private static final String REFRESH_TOKEN = "refresh_token";

    private static final String TEST_ACCESS_TOKEN = "access_token";
    private static final String TEST_DEVICE_ID = "test_device_id";

    private TokenParser tokenParserMock;
    private UserRepository userRepositoryMock;
    private TokenFactory tokenFactoryMock;
    private OneTimeAccessTokenRepository oneTimeAccessTokenRepository;
    private RefreshTokenService refreshTokenService;

    private AuthorizationRequestContext contextMock;
    private TokenInfo tokenInfo;
    private TokenGrant tokenGrant;

    @Before
    public void setUp() {
        tokenParserMock = mock(TokenParser.class);
        userRepositoryMock = mock(UserRepository.class);
        tokenFactoryMock = mock(TokenFactory.class);
        oneTimeAccessTokenRepository = mock(OneTimeAccessTokenRepository.class);
        refreshTokenService = new DefaultRefreshTokenService(tokenParserMock, userRepositoryMock, tokenFactoryMock,
                REFRESH_TOKEN_DURATION_IN_SECONDS, oneTimeAccessTokenRepository);

        contextMock = mock(AuthorizationRequestContext.class);
        when(contextMock.getIssuerClientId()).thenReturn(TEST_CLIENT);

        User userMock = mock(User.class);
        when(userMock.getId()).thenReturn(TEST_USER);
        when(contextMock.getPrincipal()).thenReturn(userMock);
        when(contextMock.getDeviceId()).thenReturn(TEST_DEVICE_ID);


        tokenGrant = new TokenGrant(REFRESH_TOKEN, REFRESH_TOKEN_DURATION_IN_SECONDS);

        tokenInfo = TokenInfo.newBuilder().setType(TokenType.REFRESH).setState(Long.toString(System.currentTimeMillis()))
                .setClientId(TEST_CLIENT).setOneUseToken(true).setUserId(TEST_USER).setDeviceId(TEST_DEVICE_ID).build();
    }

    @Test
    public void testCreateRefreshTokenWithoutUser() {
        when(contextMock.hasPrincipal()).thenReturn(false);
        String refreshToken = refreshTokenService.createRefreshToken(contextMock, TEST_ACCESS_TOKEN);
        assertThat(refreshToken).isNull();
    }

    @Test
    public void testCreateRefreshToken() {
        when(contextMock.hasPrincipal()).thenReturn(true);
        User principal = new User();
        principal.setId(TEST_USER);
        when(contextMock.getPrincipal()).thenReturn(principal);
        when(tokenFactoryMock.createToken(Mockito.any(TokenInfo.class), Mockito.eq(REFRESH_TOKEN_DURATION_IN_SECONDS),
                Mockito.eq("user:" + TEST_USER), Mockito.eq("access_token:" + TEST_ACCESS_TOKEN))).thenReturn(tokenGrant);
        String refreshToken = refreshTokenService.createRefreshToken(contextMock, TEST_ACCESS_TOKEN);
        assertThat(refreshToken).isEqualTo(tokenGrant.getAccessToken());
    }

    @Test
    public void testGetUserFromRefreshToken() throws TokenVerificationException {
        when(contextMock.hasPrincipal()).thenReturn(true);
        TokenReader tokenReaderMock = mock(TokenReader.class);
        when(tokenParserMock.parseAndVerify(REFRESH_TOKEN)).thenReturn(tokenReaderMock);
        when(tokenReaderMock.getInfo()).thenReturn(tokenInfo);
        when(userRepositoryMock.findOne(TEST_USER)).thenReturn(new User());
        User user = refreshTokenService.getUserFromRefreshToken(REFRESH_TOKEN);
        assertThat(user).isNotNull();
    }

    @Test(expected = TokenVerificationException.class)
    public void testBadRefreshToken() throws TokenVerificationException {
        when(tokenParserMock.parseAndVerify(REFRESH_TOKEN))
                .thenThrow(new TokenVerificationException("Invalid token", new IllegalArgumentException()));
        refreshTokenService.getUserFromRefreshToken(REFRESH_TOKEN);
    }

    @Test
    public void testInvalidateAllRefreshTokens() {
        refreshTokenService.invalidateRefreshToken(TEST_USER);
        verify(oneTimeAccessTokenRepository).deleteByTags("user:" + TEST_USER);
    }

    @Test
    public void testInvalidateOneRefreshTokens() {
        refreshTokenService.invalidateRefreshToken(TEST_USER, Optional.of(TEST_ACCESS_TOKEN));
        verify(oneTimeAccessTokenRepository).deleteByTags("user:" + TEST_USER, "access_token:" + TEST_ACCESS_TOKEN);
    }
}
