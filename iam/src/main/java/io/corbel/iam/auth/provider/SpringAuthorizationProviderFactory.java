package io.corbel.iam.auth.provider;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.corbel.iam.model.Domain;

/**
 * @author Rubén Carrasco
 * 
 */
public class SpringAuthorizationProviderFactory implements ApplicationContextAware, AuthorizationProviderFactory {

    private ApplicationContext applicationContext;

    @Override
    public Provider getProvider(Domain domain, String oAuthService) {
        Map<String, String> configuration = domain.getAuthConfigurations().get(oAuthService);
        if (configuration == null) {
            throw new IllegalArgumentException("Unavailable provider : " + oAuthService);
        }
        String type = configuration.get("type");
        Provider provider = applicationContext.getBean(type, Provider.class);
        provider.setConfiguration(configuration);
        return provider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
