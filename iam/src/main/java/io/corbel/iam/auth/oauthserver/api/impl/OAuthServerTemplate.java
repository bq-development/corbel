package io.corbel.iam.auth.oauthserver.api.impl;

import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;

import io.corbel.iam.auth.oauthserver.api.OAuthServer;
import io.corbel.iam.auth.oauthserver.api.UserOperations;

public class OAuthServerTemplate extends AbstractOAuth2ApiBinding implements OAuthServer {

    private final UserOperations userOperations;

    public OAuthServerTemplate(String accessToken, String domainUrl) {
        super(accessToken);
        userOperations = new UserTemplate(getRestTemplate(), domainUrl);
    }

    @Override
    public UserOperations userOperations() {
        return userOperations;
    }
}
