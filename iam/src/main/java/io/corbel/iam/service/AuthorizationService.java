package io.corbel.iam.service;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.MissingBasicParamsException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.TokenGrant;

/**
 * @author Alexander De Leon
 * 
 */
public interface AuthorizationService {

    TokenGrant authorize(String assertion) throws UnauthorizedException, MissingOAuthParamsException, OauthServerConnectionException,
            MissingBasicParamsException;

    TokenGrant authorize(String assertion, OauthParams params) throws UnauthorizedException, MissingOAuthParamsException,
            OauthServerConnectionException;

}
