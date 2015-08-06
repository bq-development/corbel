package io.corbel.iam.auth.google.api.impl;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.util.StringUtils.hasText;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.social.MissingAuthorizationException;
import org.springframework.web.client.RestTemplate;

import io.corbel.iam.auth.google.api.ApiEntity;

/**
 * Abstract superclass for implementations that work with Google+ APIs.
 * 
 * @author Gabriel Axel
 */
public abstract class AbstractGoogleApiOperations {

    protected final RestTemplate restTemplate;
    protected final boolean isAuthorized;

    protected AbstractGoogleApiOperations(RestTemplate restTemplate, boolean isAuthorized) {
        this.restTemplate = restTemplate;
        this.isAuthorized = isAuthorized;
    }

    protected void requireAuthorization() {
        if (!isAuthorized) {
            throw new MissingAuthorizationException("Google+");
        }
    }

    protected <T> T getEntity(String url, Class<T> type) {
        return restTemplate.getForObject(url, type);
    }

    @SuppressWarnings("unchecked")
    protected <T> T saveEntity(String url, T entity) {
        return (T) restTemplate.postForObject(url, entity, entity.getClass());
    }

    protected <T extends ApiEntity> T saveEntity(String baseUrl, T entity) {

        String url;
        HttpMethod method;

        if (hasText(entity.getId())) {
            url = baseUrl + '/' + entity.getId();
            method = PUT;
        } else {
            url = baseUrl;
            method = POST;
        }

        @SuppressWarnings("unchecked")
        ResponseEntity<T> response = restTemplate.exchange(url, method, new HttpEntity<T>(entity), (Class<T>) entity.getClass());

        return response.getBody();
    }

    protected void deleteEntity(String baseUrl, ApiEntity entity) {
        deleteEntity(baseUrl, entity.getId());
    }

    protected void deleteEntity(String baseUrl, String id) {
        restTemplate.delete(baseUrl + '/' + id);
    }

    protected <T> T patch(String url, Object request, Class<T> responseType) {
        ResponseEntity<T> response = restTemplate.exchange(url, PATCH, new HttpEntity<Object>(request), responseType);
        return response.getBody();
    }
}
