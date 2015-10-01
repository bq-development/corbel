package io.corbel.oauth.filter;

import io.corbel.oauth.filter.exception.AuthFilterException;

import javax.ws.rs.core.MultivaluedMap;

public interface AuthFilter {

    void filter(String username, String password, String clientId, MultivaluedMap<String, String> form) throws AuthFilterException;

    String getDomain();
}
