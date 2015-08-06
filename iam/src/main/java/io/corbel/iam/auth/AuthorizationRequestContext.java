package io.corbel.iam.auth;

import java.util.Set;

import io.corbel.iam.model.Client;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Scope;
import io.corbel.iam.model.User;

/**
 * @author Alexander De Leon
 * 
 */
public interface AuthorizationRequestContext {

    String getIssuerClientId();

    Client getIssuerClient();

    String getDeviceId();

    Domain getIssuerClientDomain();

    Domain getRequestedDomain();

    boolean isCrossDomain();

    User getPrincipal();

    User getPrincipal(String principalId);

    String getPrincipalId();

    void setPrincipalId(String username);

    Set<String> getRequestedScopes();

    boolean hasPrincipal();

    Long getAuthorizationExpiration();

    boolean isOAuth();

    String getOAuthService();

    OauthParams getOauthParams();

    boolean hasRefreshToken();

    String getRefreshToken();

    boolean hasVersion();

    String getVersion();

    boolean isBasic();

    BasicParams getBasicParams();

    Set<Scope> getExpandedRequestedScopes();

    void setExpandedRequestedScopes(Set<Scope> expandedRequestedScopes);

}
