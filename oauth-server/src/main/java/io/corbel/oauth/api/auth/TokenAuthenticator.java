package io.corbel.oauth.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.reader.TokenReader;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

/**
 * @author Alexander De Leon
 * 
 */
public class TokenAuthenticator implements Authenticator<String, TokenReader> {

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthenticator.class);

    private final TokenParser tokenParser;

    public TokenAuthenticator(TokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    public Optional<TokenReader> authenticate(String token) throws AuthenticationException {
        try {
            TokenReader tokenReader = tokenParser.parseAndVerify(token);
            return Optional.of(tokenReader);
        } catch (TokenVerificationException e) {
            LOG.debug("Token verification failed: {}", e.getMessage());
            return Optional.absent();
        }

    }
}
