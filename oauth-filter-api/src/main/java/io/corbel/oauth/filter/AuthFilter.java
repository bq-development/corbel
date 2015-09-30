package io.corbel.oauth.filter;

import io.corbel.oauth.filter.exception.AuthFilterException;

import javax.ws.rs.core.MultivaluedMap;

// NOTE: Put child filters in "io.corbel.oauth.filter" or "com.bqreaders.silkroad.oauth.filter" package and mark it with @Component.
public interface AuthFilter {

    void filter(String username, String password, String clientId, MultivaluedMap<String, String> form) throws AuthFilterException;

    String getDomain();
}
