package io.corbel.iam.auth;

import io.corbel.iam.exception.UnauthorizedException;

/**
 * An {@link AuthorizationRule} validates one aspect of the authorization request process. In the execution of its process method, the rule
 * can throw an {@link UnauthorizedException} making the authorization request fail immediately.
 * 
 * @author Alexander De Leon
 * 
 */
public interface AuthorizationRule {

    void process(AuthorizationRequestContext context) throws UnauthorizedException;

}
