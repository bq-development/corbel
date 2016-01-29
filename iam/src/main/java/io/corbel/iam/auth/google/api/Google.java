package io.corbel.iam.auth.google.api;

import org.springframework.social.ApiBinding;

import io.corbel.iam.auth.google.api.userinfo.UserInfoOperations;

public interface Google extends ApiBinding {

    UserInfoOperations userOperations();

    String getAccessToken();
}
