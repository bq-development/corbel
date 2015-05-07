package com.bq.oss.corbel.iam.auth.google.api;

import org.springframework.social.ApiBinding;

import com.bq.oss.corbel.iam.auth.google.api.impl.GoogleTemplate;
import com.bq.oss.corbel.iam.auth.google.api.userinfo.UserInfoOperations;

public interface Google extends ApiBinding {

    UserInfoOperations userOperations();

    String getAccessToken();
}
