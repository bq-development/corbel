package io.corbel.oauth.token.verifier;

import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.token.verifier.TokenVerifier;
import io.corbel.oauth.repository.UserRepository;

/**
 * @author Francisco Sanchez
 */
public class UserExistsTokenVerifier implements TokenVerifier {

    private final UserRepository userRepository;

    public UserExistsTokenVerifier(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void verify(TokenReader reader) throws TokenVerificationException {
        if (userRepository.findOne(reader.getInfo().getUserId()) == null) {
            throw new TokenVerificationException.UserNotExists();
        }
    }
}
