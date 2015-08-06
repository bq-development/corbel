package io.corbel.iam.auth.google.connect;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;

import io.corbel.iam.auth.google.api.Google;

public class GoogleConnectionFactory extends OAuth2ConnectionFactory<Google> {

    public GoogleConnectionFactory(String clientId, String clientSecret) {
        super("google", new GoogleServiceProvider(clientId, clientSecret), new GoogleAdapter());
    }

    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        Google api = ((GoogleServiceProvider) getServiceProvider()).getApi(accessGrant.getAccessToken());
        UserProfile userProfile = getApiAdapter().fetchUserProfile(api);
        return userProfile.getUsername();
    }
}
