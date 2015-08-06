package io.corbel.iam.auth.oauthserver.connect;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;

import io.corbel.iam.auth.oauthserver.api.OAuthServer;

public class OAuthServerConnectionFactory extends OAuth2ConnectionFactory<OAuthServer> {

    public OAuthServerConnectionFactory(String clientId, String clientSecret, String domainUrl) {
        super("corbel", new OAuthServerServiceProvider(clientId, clientSecret, domainUrl), new OAuthServerAdapter());
    }
}
