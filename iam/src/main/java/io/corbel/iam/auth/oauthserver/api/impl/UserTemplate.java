package io.corbel.iam.auth.oauthserver.api.impl;

import org.springframework.web.client.RestTemplate;

import io.corbel.iam.auth.oauthserver.api.Endpoint;
import io.corbel.iam.auth.oauthserver.api.UserOperations;

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
