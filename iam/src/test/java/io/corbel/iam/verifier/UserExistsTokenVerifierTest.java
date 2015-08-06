package io.corbel.iam.verifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.corbel.iam.model.User;
import io.corbel.iam.repository.UserRepository;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.reader.TokenReader;

/**
 * @author Alberto J. Rubio
 * 
 */
public class UserExistsTokenVerifierTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private UserExistsTokenVerifier verifier;

    @Before
    public void setup() {
        verifier = new UserExistsTokenVerifier(userRepository);
    }

    @Test(expected = TokenVerificationException.UserNotExists.class)
    public void testUserNotExists() throws TokenVerificationException {
        TokenReader reader = mock(TokenReader.class);
        TokenInfo tokenInfo = mock(TokenInfo.class);
        when(reader.getInfo()).thenReturn(tokenInfo);
        when(tokenInfo.getUserId()).thenReturn("username");
        when(userRepository.findOne("username")).thenReturn(null);
        verifier.verify(reader);
    }

    @Test
    public void testOk() throws TokenVerificationException {
        TokenReader reader = mock(TokenReader.class);
        TokenInfo tokenInfo = mock(TokenInfo.class);
        when(reader.getInfo()).thenReturn(tokenInfo);
        when(tokenInfo.getUserId()).thenReturn("username");
        when(userRepository.findOne("username")).thenReturn(new User());
        verifier.verify(reader);
    }

}
