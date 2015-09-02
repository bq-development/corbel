package io.corbel.iam.api;

import javax.ws.rs.core.Response;

import io.corbel.iam.utils.Message;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;

/**
 * @author Alexander De Leon
 * 
 */
public final class IamErrorResponseFactory extends ErrorResponseFactory {

    private static final Error MISSING_OAUTH_PARAM_ERROR = new Error("missing_oauth_params", Message.MISSING_OAUTH_PARAM.getMessage());
    private static final Error MISSING_ASSERTION_ERROR = new Error("invalid_grant", Message.MISSING_ASSERTION.getMessage());
    private static final Error INVALID_GRANT_ERROR = new Error("invalid_grant", Message.MISSING_GRANT_TYPE.getMessage());
    private static final Error MISSING_BASIC_PARAM_ERROR = new Error("missing_basic_param_error", Message.MISSING_BASIC_PARAM.getMessage());

    private static IamErrorResponseFactory instance;

    private IamErrorResponseFactory() {}

    public static IamErrorResponseFactory getInstance() {
        if (instance == null) {
            instance = new IamErrorResponseFactory();
        }
        return instance;
    }

    public Response missingGrantType() {
        return badRequest(INVALID_GRANT_ERROR);
    }

    public Response missingAssertion() {
        return badRequest(MISSING_ASSERTION_ERROR);
    }

    public Response notSupportedGrantType(String grantType) {
        return badRequest(new Error("invalid_grant", Message.NOT_SUPPORTED_GRANT_TYPE.getMessage(grantType)));
    }

    public Response noSuchPrincipal(String message) {
        return unauthorized(new Error("no_such_principal", message));
    }

    public Response unsupportedVersion(String message) {
        return forbidden(new Error("unsupported_version", message));
    }

    public Response entityExists(Message message, String... params) {
        return conflict(new Error("entity_exists", message.getMessage((Object[]) params)));
    }

    public Response identityExists(Message message, String... params) {
        return conflict(new Error("identity_exists", message.getMessage((Object[]) params)));
    }

    public Response oauthServiceDuplicated(Message message, String... params) {
        return conflict(new Error("oauth_service_duplicated", message.getMessage((Object[]) params)));
    }

    public Response scopesNotAllowed(String domain) {
        return forbidden(new Error("scopes_not_allowed", Message.SCOPES_NOT_ALLOWED.getMessage(domain)));
    }

    public Response scopesNotExist(String scopes) {
        return badRequest(new Error("not_existent_scope", scopes));
    }

    public Response domainQueryParamNotAllowed() {
        return badRequest(new Error("domain_query_param_not_allowed", Message.DOMAIN_QUERY_PARAM_NOT_ALLOWED.getMessage()));
    }

    public Response missingOauthParms() {
        return badRequest(MISSING_OAUTH_PARAM_ERROR);
    }

    public Response invalidOAuthService(String domain) {
        return badRequest(new Error("invalid_oauth_service", Message.INVALID_OAUTH_SERVICE.getMessage(domain)));
    }

    public Response invalidArgument(String invalidArgument) {
        return badRequest(new Error("invalid_argument", invalidArgument));
    }

    public Response missingBasicParms() {
        return badRequest(MISSING_BASIC_PARAM_ERROR);
    }

    public Response scopeIdNotAllowed(String scopeId) {
        return badRequest(new Error("scope_id_not_allowed", Message.SCOPE_ID_NOT_ALLOWED.getMessage(scopeId)));
    }

    public Response domainNotExists(String domainId) {
        return badRequest(new Error("domain_not_exists", Message.DOMAIN_NOT_EXISTS.getMessage(domainId)));
    }

    public Response groupNotExists(String groupId) {
        return notfound(new Error("group_not_exists", Message.GROUP_NOT_EXISTS.getMessage(groupId)));
    }

    public Response groupAlreadyExists(String nameAndDomain) {
        return conflict(new Error("group_already_exists", Message.GROUP_ALREADY_EXISTS.getMessage(nameAndDomain)));
    }

    public Response unauthorizedGroupDeletion(String id) {
        return unauthorized(new Error("unauthorized_group_deletion", Message.GROUP_DELETION_UNAUTHORIZED.getMessage(id)));
    }

    public Response unauthorizedGroupUpdate(String id) {
        return unauthorized(new Error("unauthorized_group_update", Message.GROUP_UPDATE_UNAUTHORIZED.getMessage(id)));
    }

}