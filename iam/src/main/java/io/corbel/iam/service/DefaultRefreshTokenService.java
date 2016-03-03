package io.corbel.iam.service;

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.UserRepository;
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
public class DefaultRefreshTokenService implements RefreshTokenService {

    private static final String ACCESS_TOKEN_TAG_TEMPLATE = "access_token:{0}";
    private static final String USER_TAG_TEMPLATE = "user:{0}";

    private final TokenParser tokenParser;
    private final UserRepository userRepository;
    private final TokenFactory refreshTokenFactory;
    private final long refreshTokenDurationInSeconds;
    private final OneTimeAccessTokenRepository oneTimeAccessTokenRepository;

    public DefaultRefreshTokenService(TokenParser tokenParser, UserRepository userRepository, TokenFactory refreshTokenFactory,
            long refreshTokenDurationInSeconds, OneTimeAccessTokenRepository oneTimeAccessTokenRepository) {
        this.tokenParser = tokenParser;
        this.userRepository = userRepository;
        this.refreshTokenFactory = refreshTokenFactory;
        this.refreshTokenDurationInSeconds = refreshTokenDurationInSeconds;
        this.oneTimeAccessTokenRepository = oneTimeAccessTokenRepository;
    }

    @Override
    public String createRefreshToken(AuthorizationRequestContext context, String accessToken) {
        String refreshToken = null;
        if (context.hasPrincipal()) {
            String deviceId = context.getDeviceId();
            TokenInfo.Builder tokenInfoBuilder = TokenInfo.newBuilder().setOneUseToken(true).setType(TokenType.REFRESH)
                    .setState(Long.toString(System.currentTimeMillis())).setClientId(context.getIssuerClientId())
                    .setUserId(context.getPrincipal().getId()).setGroups(context.getPrincipal().getGroups());

            if (!StringUtils.isEmpty(deviceId)) {
                tokenInfoBuilder.setDeviceId(deviceId);
            }

            refreshToken = refreshTokenFactory
                    .createToken(tokenInfoBuilder.build(), refreshTokenDurationInSeconds, userTag(context), accessTokenTag(accessToken))
                    .getAccessToken();
        }
        return refreshToken;
    }

    @Override
    public User getUserFromRefreshToken(String refreshToken) throws TokenVerificationException {
        TokenReader tokenReader = tokenParser.parseAndVerify(refreshToken);
        return userRepository.findOne(tokenReader.getInfo().getUserId());
    }

    @Override
    public void invalidateRefreshToken(String user, Optional<String> accessToken) {
        if (accessToken.isPresent()) {
            oneTimeAccessTokenRepository.deleteByTags(userTag(user), accessTokenTag(accessToken.get()));
        } else {
            oneTimeAccessTokenRepository.deleteByTags(userTag(user));
        }
    }

    private String accessTokenTag(String accessToken) {
        return MessageFormat.format(ACCESS_TOKEN_TAG_TEMPLATE, accessToken);
    }

    private String userTag(AuthorizationRequestContext context) {
        return userTag(context.getPrincipal().getId());
    }

    private String userTag(String userId) {
        return MessageFormat.format(USER_TAG_TEMPLATE, userId);
    }
}
