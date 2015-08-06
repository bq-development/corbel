package io.corbel.iam.verifier;

import io.corbel.iam.repository.UserRepository;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.token.verifier.TokenVerifier;

/**
 * @author Alberto J. Rubio
 */
public class UserExistsTokenVerifier implements TokenVerifier {

    private final UserRepository userRepository;

    public UserExistsTokenVerifier(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void verify(TokenReader reader) throws TokenVerificationException {
        if (reader.getInfo().getUserId() != null && userRepository.findOne(reader.getInfo().getUserId()) == null) {
            throw new TokenVerificationException.UserNotExists();
        }
    }
}
