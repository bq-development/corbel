package com.bq.oss.corbel.iam.auth.provider;

import java.util.Map;

import org.springframework.social.NotAuthorizedException;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.web.client.ResourceAccessException;

import com.bq.oss.corbel.iam.auth.OauthParams;
import com.bq.oss.corbel.iam.exception.ExchangeOauthCodeException;
import com.bq.oss.corbel.iam.exception.MissingOAuthParamsException;
import com.bq.oss.corbel.iam.exception.OauthServerConnectionException;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.repository.IdentityRepository;

/**
 * @author Rubén Carrasco
 * 
 */
public class FacebookProvider extends AbstractOAuth2Provider<Facebook> {

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
            throw new UnauthorizedException("Unable to verify identity with Facebook");
        }
        throw new UnauthorizedException("Unable to verify identity with Facebook");
    }

}
