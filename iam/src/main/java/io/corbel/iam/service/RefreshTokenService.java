package io.corbel.iam.service;

import java.util.Optional;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.model.User;
import io.corbel.lib.token.exception.TokenVerificationException;

/**
 * @author Alberto J. Rubio
 */
public interface RefreshTokenService {

    String createRefreshToken(AuthorizationRequestContext context, String accessToken);

    User getUserFromRefreshToken(String refreshToken) throws TokenVerificationException;

    void invalidateRefreshToken(String user, Optional<String> accessToken);

    default void invalidateRefreshToken(String user) {
        invalidateRefreshToken(user, Optional.empty());
    }
}
