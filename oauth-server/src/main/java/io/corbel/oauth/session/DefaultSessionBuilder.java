package io.corbel.oauth.session;

import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;

/**
 * @author Alberto J. Rubio
 */
public class DefaultSessionBuilder implements SessionBuilder {

    private final TokenFactory tokenFactory;
    private final long sessionMaxAge;

    public DefaultSessionBuilder(TokenFactory tokenFactory, long sessionMaxAge) {
        this.tokenFactory = tokenFactory;
        this.sessionMaxAge = sessionMaxAge;
    }

    @Override
    public String createNewSession(String clientId, String userId) {
        TokenInfo newSession = TokenInfo.newBuilder().setType(TokenType.REFRESH).setClientId(clientId).setUserId(userId)
                .setState(Long.toString(System.currentTimeMillis())).setOneUseToken(true).build();
        TokenGrant token = tokenFactory.createToken(newSession, sessionMaxAge);
        return token.getAccessToken();
    }
}
