package com.bq.oss.corbel.iam.auth.oauthserver.api.impl;

import org.springframework.web.client.RestTemplate;

import com.bq.oss.corbel.iam.auth.oauthserver.api.Endpoint;
import com.bq.oss.corbel.iam.auth.oauthserver.api.UserOperations;

public class UserTemplate implements UserOperations {

    private final RestTemplate restTemplate;
    private final String domainUrl;

    public UserTemplate(RestTemplate restTemplate, String domainUrl) {
        this.restTemplate = restTemplate;
        this.domainUrl = domainUrl;
    }

    @Override
    public Profile getUserProfile() {
        return restTemplate.getForObject(domainUrl + Endpoint.USERS, Profile.class);
    }

}
