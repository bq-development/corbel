package com.bq.oss.corbel.iam.auth;

import java.util.Set;

import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.model.User;

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
}
