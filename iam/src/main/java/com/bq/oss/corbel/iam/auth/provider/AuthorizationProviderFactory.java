package com.bq.oss.corbel.iam.auth.provider;

import com.bq.oss.corbel.iam.model.Domain;

/**
 * @author Rubén Carrasco
 *
 */
public interface AuthorizationProviderFactory {

    Provider getProvider(Domain domain, String oAuthService);

}