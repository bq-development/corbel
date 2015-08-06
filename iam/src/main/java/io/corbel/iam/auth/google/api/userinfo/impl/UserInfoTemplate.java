package io.corbel.iam.auth.google.api.userinfo.impl;

import org.springframework.web.client.RestTemplate;

import io.corbel.iam.auth.google.api.Endpoint;
import io.corbel.iam.auth.google.api.impl.AbstractGoogleApiOperations;
import io.corbel.iam.auth.google.api.userinfo.GoogleUserInfo;
import io.corbel.iam.auth.google.api.userinfo.UserInfoOperations;

public class UserInfoTemplate extends AbstractGoogleApiOperations implements UserInfoOperations {

    public UserInfoTemplate(RestTemplate restTemplate, boolean authorized) {
        super(restTemplate, authorized);
    }

    @Override
    public GoogleUserInfo getUserInfo() {
        requireAuthorization();
        return restTemplate.getForObject(Endpoint.USER_INFO, GoogleUserInfo.class);
    }

}
