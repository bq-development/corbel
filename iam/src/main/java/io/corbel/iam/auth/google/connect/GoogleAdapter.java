package io.corbel.iam.auth.google.connect;

import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

import io.corbel.iam.auth.google.api.Google;
import io.corbel.iam.auth.google.api.userinfo.GoogleUserInfo;

public class GoogleAdapter implements ApiAdapter<Google> {

    public boolean test(Google google) {
        return true;
    }

    public void setConnectionValues(Google google, ConnectionValues values) {
        GoogleUserInfo profile = google.userOperations().getUserInfo();
        values.setProviderUserId(profile.getId());
        values.setDisplayName(profile.getName());
        values.setProfileUrl(profile.getLink());
        values.setImageUrl(profile.getProfilePictureUrl());
    }

    public UserProfile fetchUserProfile(Google google) {
        GoogleUserInfo profile = google.userOperations().getUserInfo();
        return new UserProfileBuilder().setUsername(profile.getEmail()).setEmail(profile.getEmail()).setName(profile.getName())
                .setFirstName(profile.getFirstName()).setLastName(profile.getLastName()).build();
    }

    public void updateStatus(Google google, String message) {
        throw new UnsupportedOperationException();
    }

}
