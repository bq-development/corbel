package com.bq.oss.corbel.iam.service;

import com.bq.oss.corbel.iam.auth.OauthParams;
import com.bq.oss.corbel.iam.exception.MissingBasicParamsException;
import com.bq.oss.corbel.iam.exception.MissingOAuthParamsException;
import com.bq.oss.corbel.iam.exception.OauthServerConnectionException;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.TokenGrant;

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
