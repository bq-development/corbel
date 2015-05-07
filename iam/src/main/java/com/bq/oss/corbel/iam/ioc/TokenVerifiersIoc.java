package com.bq.oss.corbel.iam.ioc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.corbel.iam.verifier.UserExistsTokenVerifier;

/**
 * @author Alberto J. Rubio
 *
 */
@Configuration public class TokenVerifiersIoc {

    @Bean
    public UserExistsTokenVerifier userExistsTokenVerifier(UserRepository userRepository) {
        return new UserExistsTokenVerifier(userRepository);
    }
}
