package io.corbel.iam.auth.provider;

import io.corbel.iam.model.Domain;

/**
 * @author Rubén Carrasco
 *
 */
public interface AuthorizationProviderFactory {

    Provider getProvider(Domain domain, String oAuthService);

}