package com.bq.oss.corbel.iam.verifier;

import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.lib.token.exception.TokenVerificationException;
import com.bq.oss.lib.token.reader.TokenReader;
import com.bq.oss.lib.token.verifier.TokenVerifier;

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
