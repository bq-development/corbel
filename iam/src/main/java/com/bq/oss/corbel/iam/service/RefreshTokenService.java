package com.bq.oss.corbel.iam.service;

import java.util.Optional;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.lib.token.exception.TokenVerificationException;

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
