package io.corbel.iam.auth.provider;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.ExchangeOauthCodeException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.repository.IdentityRepository;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.web.client.ResourceAccessException;

/**
 * @author Rubén Carrasco
 * 
 */
public class FacebookProvider extends AbstractOAuth2Provider<Facebook> {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookProvider.class);

    public FacebookProvider(IdentityRepository identityRepository) {
        super(identityRepository);
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        super.setConfiguration(configuration);
        connectionFactory = new FacebookConnectionFactory(configuration.get("clientId"), configuration.get("clientSecret"));
    }

    @Override
    public Identity getIdentity(OauthParams params, String oAuthService, String domain) throws UnauthorizedException,
            MissingOAuthParamsException, ExchangeOauthCodeException, OauthServerConnectionException {
        try {
            AccessGrant accessGrant = getAccessGrant(params);
            Connection<Facebook> connection = connectionFactory.createConnection(accessGrant);
            if (connection != null && !connection.hasExpired()) {
                Facebook facebook = connection.getApi();
                return identityRepository.findByOauthIdAndDomainAndOauthService(facebook.userOperations().getUserProfile().getId(), domain,
                        oAuthService);
            }
        } catch (ExchangeOauthCodeException e) {
            throw new UnauthorizedException("Unable to exchange code with Facebook: " + e.getMessage());
        } catch (ResourceAccessException e) {
            throw new OauthServerConnectionException("facebook", e.getMessage());
        } catch (NotAuthorizedException e) {
            throw new UnauthorizedException("Unable to verify identity with Facebook:  401 Unauthorized");
        } catch (Exception e) {
            LOG.error("Unexpected error when verify identity with Facebook", e);
            throw new UnauthorizedException("Unable to verify identity with Facebook");
        }
        LOG.error("Facebook connection failed or expired");
        throw new UnauthorizedException("Unable to verify identity with Facebook");
    }

}
