package com.bq.oss.corbel.iam.auth.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.web.client.ResourceAccessException;

import com.bq.oss.corbel.iam.auth.OauthParams;
import com.bq.oss.corbel.iam.auth.google.api.Google;
import com.bq.oss.corbel.iam.auth.google.connect.GoogleConnectionFactory;
import com.bq.oss.corbel.iam.exception.ExchangeOauthCodeException;
import com.bq.oss.corbel.iam.exception.MissingOAuthParamsException;
import com.bq.oss.corbel.iam.exception.OauthServerConnectionException;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.repository.IdentityRepository;

public class GoogleProvider extends AbstractOAuth2Provider<Google> {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleProvider.class);

    private static String REQUIRED_SCOPES = "profile email";

    public GoogleProvider(IdentityRepository identityRepository) {
        super(identityRepository);
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        super.setConfiguration(configuration);
        connectionFactory = new GoogleConnectionFactory(configuration.get("clientId"), configuration.get("clientSecret"));
    }

    @Override
    public Identity getIdentity(OauthParams params, String oAuthService, String domain) throws UnauthorizedException,
            MissingOAuthParamsException, ExchangeOauthCodeException, OauthServerConnectionException {
        try {
            AccessGrant accessGrant = getAccessGrant(params);
            Connection<Google> connection = connectionFactory.createConnection(accessGrant);
            if (connection != null && !connection.hasExpired()) {
                Google google = connection.getApi();
                return identityRepository.findByOauthIdAndDomainAndOauthService(google.userOperations().getUserInfo().getId(), domain,
                        oAuthService);
            }
        } catch (ExchangeOauthCodeException e) {
            throw new UnauthorizedException("Unable to exchange code with Google: " + e.getMessage());
        } catch (ResourceAccessException e) {
            throw new OauthServerConnectionException("google", e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedException("Unable to verify identity with Google: " + e.getMessage());
        }
        throw new UnauthorizedException("Unable to verify identity with Google");
    }

    @Override
    public String getAuthUrl(String assertion) {
        OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuth2Parameters params = new OAuth2Parameters();
        params.setRedirectUri(redirectUri);
        try {
            // TODO assertion by state? Google do not allow to send query params in redirect_uri
            params.setState(URLEncoder.encode(ASSERTION + "=" + assertion, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("UTF-8 not supported");
        }
        params.setScope(REQUIRED_SCOPES);
        String authorizeUrl = oauthOperations.buildAuthorizeUrl(GrantType.IMPLICIT_GRANT, params);
        return authorizeUrl;
    }

}
