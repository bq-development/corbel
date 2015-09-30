package io.corbel.oauth.filter;

import io.corbel.oauth.filter.exception.AuthFilterException;

import javax.ws.rs.core.MultivaluedMap;


public interface FilterRegistry {

    void registerFilter(AuthFilter filter);

    void filter(String username, String password, String clientId, String domain, MultivaluedMap<String, String> form)
            throws AuthFilterException;
}
