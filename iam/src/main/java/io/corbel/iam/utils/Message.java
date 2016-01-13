package io.corbel.iam.utils;

import java.text.MessageFormat;

/**
 * @author Alberto J. Rubio
 */
public enum Message {

    PRINCIPAL_EXISTS_UNAUTHORIZED("Principal {0} does not exists in domain {1}"),
    OAUTH_PRINCIPAL_EXISTS_UNAUTHORIZED("Principal from OAuth Service {0} does not exists in domain {1}"),
    MISSING_GRANT_TYPE("Missing grant_type"),
    MISSING_ASSERTION("Missing assertion"),
    NOT_FOUND("Not found"),
    NOT_SUPPORTED_GRANT_TYPE("Non-supported grant_type : {0}"),
    REQUESTED_SCOPES_UNAUTHORIZED("Unauthorized scopes : {0}"),
    DOMAIN_QUERY_PARAM_NOT_ALLOWED("Domain query param is not allowed"),
    USER_EXISTS("{0} duplicated"),
    IDENTITY_EXITS("Identity with id {0} and service {1} already exists in domain {2}"),
    DUPLICATED_OAUTH_SERVICE_IDENTITY("User {0} already has an identity with service {1} in domain {2}"),
    MISSING_OAUTH_PARAM("Missing oauth parameters"),
    INVALID_OAUTH_SERVICE("OAuth service not allowed in domain {0}"),
    SCOPES_NOT_ALLOWED("Some specified scopes are not allowed in the domain {0}"),
    INVALID_VERSION("Version {0} is not supported. Versions supported: {1}"),
    MISSING_IDENTITY_PROOF("Missing identity proof"),
    CLIENT_SIDE_AUTHENTICATION_NOT_ALLOWED("The client side authentication is not allowed in the issuer client"),
    AUTHENTICATION_TYPE_NOT_ALLOWED("The authentication type is not allowed in the requested domain"),
    REQUESTED_DOMAIN_NOT_ALLOWED("The requested domain is not allowed for the client root domain"),
    UNKNOWN_BASIC_USER_CREDENTIALS("Unknown basic user credentials."),
    MISSING_BASIC_PARAM("Missing basic parameters."),
    CLIENT_EXISTS("Client duplicated"),
    DOMAIN_EXISTS("Domain duplicated"),
    DOMAIN_NOT_EXISTS("Domain {0} not exists"),
    SCOPE_ID_NOT_ALLOWED("Scope id not allowed: \"{0}\""),
    INVALID_DOMAIN_ID("Domain id can't contain : character."),
    GROUP_NOT_EXISTS("Group {0} not exists"),
    GROUP_ALREADY_EXISTS("Already existing group {0}"),
    GROUP_DELETION_UNAUTHORIZED("Authorization denied for deletion of group {0}"),
    GROUP_UPDATE_UNAUTHORIZED("Authorization denied for update of group {0}");

    private final String pattern;

    Message(String pattern) {
        this.pattern = pattern;
    }

    public String getMessage(Object... params) {
        return MessageFormat.format(pattern, params);
    }

}
