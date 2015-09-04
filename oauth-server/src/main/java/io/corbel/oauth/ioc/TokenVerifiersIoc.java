package io.corbel.oauth.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.corbel.oauth.repository.UserRepository;
import io.corbel.oauth.token.verifier.UserExistsTokenVerifier;

/**
 * @author Alexander De Leon
 *
 */
@Configuration public class TokenVerifiersIoc {

    @Bean
    public UserExistsTokenVerifier userExistsTokenVerifier(UserRepository userRepository) {
        return new UserExistsTokenVerifier(userRepository);
    }

}
