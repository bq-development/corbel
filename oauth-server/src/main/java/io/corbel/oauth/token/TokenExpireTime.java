package io.corbel.oauth.token;

import io.corbel.lib.token.model.TokenType;

/**
 * @author Cristian del Cerro
 */
public class TokenExpireTime {

    private final long codeAccessTokenDurationInSec;
    private final long accessTokenDurationInSec;

    public TokenExpireTime(long codeAccessTokenDurationInSec, long accessTokenDurationInSec) {
        this.codeAccessTokenDurationInSec = codeAccessTokenDurationInSec;
        this.accessTokenDurationInSec = accessTokenDurationInSec;
    }

    public long getTokenExpireTimeFromResponseType(TokenType type) {
        switch (type) {
            case CODE:
                return codeAccessTokenDurationInSec;
            case TOKEN:
                return accessTokenDurationInSec;
            default:
                return 0;
        }
    }
}
