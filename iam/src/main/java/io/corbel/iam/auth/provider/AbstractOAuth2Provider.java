package io.corbel.iam.auth.provider;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.web.client.HttpClientErrorException;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.ExchangeOauthCodeException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.repository.IdentityRepository;

public abstract class AbstractOAuth2Provider<T> implements Provider {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOAuth2Provider.class);

    protected static final String ASSERTION = "assertion";

    protected OAuth2ConnectionFactory<T> connectionFactory;
    protected String redirectUri;
    protected final IdentityRepository identityRepository;

    public AbstractOAuth2Provider(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.redirectUri = configuration.get("redirectUri");
    }

    protected AccessGrant getAccessGrant(OauthParams params) throws ExchangeOauthCodeException, MissingOAuthParamsException {

        validateParams(params);

        if (params.getCode() != null) {
            return exchangeForAccessGrant(params);
        } else {
            return new AccessGrant(params.getAccessToken());
        }
    }

    private AccessGrant exchangeForAccessGrant(OauthParams params) throws ExchangeOauthCodeException {
        try {
            return connectionFactory.getOAuthOperations().exchangeForAccess(params.getCode(), params.getRedirectUri(), null);
        } catch (HttpClientErrorException e) {
            LOG.warn("Unexpected HTTP error response when exchanging oauth code: " + e.getMessage(), e);
            throw new ExchangeOauthCodeException("Unable to exchange oauth code");
        }
    }

    private void validateParams(OauthParams params) throws MissingOAuthParamsException {
        if (params.getCode() != null && params.getRedirectUri() == null) {
            throwMissingParameterException("redirectUri");
        } else if (params.getCode() == null && params.getRedirectUri() != null) {
            throwMissingParameterException("code");
        } else if (params.getCode() == null && params.getAccessToken() == null) {
            throwMissingParameterException("code or token");
        }

    }

    private void throwMissingParameterException(String parameter) throws MissingOAuthParamsException {
        throw new MissingOAuthParamsException("Missing parameter: " + parameter);
    }

    @Override
    public String getAuthUrl(String assertion) {
        OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuth2Parameters params = new OAuth2Parameters();
        params.setRedirectUri(UrlGenerator.generateUrl(redirectUri, ASSERTION, assertion));
        String authorizeUrl = oauthOperations.buildAuthorizeUrl(GrantType.IMPLICIT_GRANT, params);
        return authorizeUrl;
    }
}
