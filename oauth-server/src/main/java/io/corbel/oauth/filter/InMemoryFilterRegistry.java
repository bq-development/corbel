package io.corbel.oauth.filter;

import io.corbel.oauth.filter.exception.AuthFilterException;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

public class InMemoryFilterRegistry implements FilterRegistry {

    private final Set<AuthFilter> filters;

    public InMemoryFilterRegistry() {
        this.filters = new HashSet<AuthFilter>();
    }

    @Override
    public void registerFilter(AuthFilter filter) {
        filters.add(filter);
    }

    @Override
    public void filter(String username, String password, String clientId, String domain, MultivaluedMap<String, String> form)
            throws AuthFilterException {
        for (AuthFilter filter : filters) {
            if (filter.getDomain().equals(domain)) {
                filter.filter(username, password, clientId, form);
            }
        }
    }
}
