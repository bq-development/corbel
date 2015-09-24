package io.corbel.oauth.filter;

import javax.ws.rs.core.MultivaluedMap;

// NOTE: Put child filters in "io.corbel.oauth.filter" or "com.bqreaders.silkroad.oauth.filter" package and mark it with @Component.
public interface AuthFilter {

    boolean filter(String username, String password, String clientId, MultivaluedMap<String, String> form);

    String getDomain();
}
