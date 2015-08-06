package io.corbel.iam.auth.oauthserver.connect;

import org.springframework.social.ApiException;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

import io.corbel.iam.auth.oauthserver.api.OAuthServer;
import io.corbel.iam.auth.oauthserver.api.impl.Profile;

public class OAuthServerAdapter implements ApiAdapter<OAuthServer> {

    @Override
    public boolean test(OAuthServer api) {
        try {
            api.userOperations().getUserProfile();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public void setConnectionValues(OAuthServer api, ConnectionValues values) {
        values.setProviderUserId(api.userOperations().getUserProfile().getId());
    }

    @Override
    public UserProfile fetchUserProfile(OAuthServer api) {
        Profile profile = api.userOperations().getUserProfile();
        return new UserProfileBuilder().setEmail(profile.getEmail()).setUsername(profile.getUsername()).build();
    }

    @Override
    public void updateStatus(OAuthServer api, String message) {
        // Nothing to do here...
    }

}
