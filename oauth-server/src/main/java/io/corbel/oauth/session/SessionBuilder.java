package io.corbel.oauth.session;

/**
 * @author Alberto J. Rubio
 */
public interface SessionBuilder {

    String createNewSession(String clientId, String userId);
}
