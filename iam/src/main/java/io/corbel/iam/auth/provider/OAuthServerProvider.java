package io.corbel.iam.auth.provider;

import java.util.Map;

import org.springframework.social.connect.Connection;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.web.client.ResourceAccessException;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.auth.oauthserver.api.OAuthServer;
import io.corbel.iam.auth.oauthserver.connect.OAuthServerConnectionFactory;
import io.corbel.iam.exception.ExchangeOauthCodeException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.repository.IdentityRepository;

public class OAuthServerProvider extends AbstractOAuth2Provider<OAuthServer> {

    public OAuthServerProvider(IdentityRepository identityRepository) {
        super(identityRepository);
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        super.setConfiguration(configuration);
        connectionFactory = new OAuthServerConnectionFactory(configuration.get("clientId"), configuration.get("clientSecret"),
                configuration.get("oAuthServerUrl"));
    }

    @Override
    public Identity getIdentity(OauthParams params, String oAuthService, String domain) throws UnauthorizedException,
            MissingOAuthParamsException, OauthServerConnectionException {
        try {
            AccessGrant accessGrant = getAccessGrant(params);
            Connection<OAuthServer> connection = connectionFactory.createConnection(accessGrant);
            if (connection != null && !connection.hasExpired()) {
                OAuthServer corbel = connection.getApi();
                return identityRepository.findByOauthIdAndDomainAndOauthService(corbel.userOperations().getUserProfile().getId(), domain,
                        oAuthService);
            }
        } catch (ExchangeOauthCodeException e) {
            throw new UnauthorizedException("Unable to exchange code with Corbel OAuth Server: " + e.getMessage());
        } catch (ResourceAccessException e) {
            throw new OauthServerConnectionException("corbel", e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedException("Unable to verify identity with Corbel OAuth Server: " + e.getMessage());
        }
        throw new UnauthorizedException("Unable to verify identity with Corbel OAuth Server");

    }

}
