package io.corbel.iam.ioc;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.corbel.iam.auth.provider.*;
import io.corbel.iam.repository.IdentityRepository;

/**
 * @author Alberto J. Rubio
 */
@Configuration public class IamProviderIoc {

    @Bean(name = "facebook")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getFacebookProvider(IdentityRepository identityRepository) {
        return new FacebookProvider(identityRepository);

    }

    @Bean(name = "twitter")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getTwitterProvider(IdentityRepository identityRepository) {
        return new TwitterProvider(identityRepository);

    }

    @Bean(name = "google")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getGoogleProvider(IdentityRepository identityRepository) {
        return new GoogleProvider(identityRepository);
    }

    @Bean(name = "oauth-server")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getCorbelProvider(IdentityRepository identityRepository) {
        return new OAuthServerProvider(identityRepository);
    }

}
