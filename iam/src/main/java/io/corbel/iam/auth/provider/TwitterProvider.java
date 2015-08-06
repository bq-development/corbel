package io.corbel.iam.auth.provider;

import java.util.Map;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import org.springframework.web.client.HttpClientErrorException;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.repository.IdentityRepository;

/**
 * @author Rubén Carrasco
 * 
 */
public class TwitterProvider implements Provider {

    private static final String ASSERTION = "assertion";
    protected OAuth1ConnectionFactory<Twitter> connectionFactory;
    private String redirectUri;
    private String secret;
    private final IdentityRepository identityRepository;

    public TwitterProvider(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public void setConfiguration(Map<String, String> configuration) {
        this.redirectUri = configuration.get("redirectUri");
        connectionFactory = new TwitterConnectionFactory(configuration.get("consumerKey"), configuration.get("consumerSecret"));
        this.secret = configuration.get("consumerSecret");

    }

    @Override
    public String getAuthUrl(String assertion) {
        OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuth1Parameters params = new OAuth1Parameters();
        String generatedUrl = UrlGenerator.generateUrl(redirectUri, ASSERTION, assertion);
        params.setCallbackUrl(generatedUrl);
        OAuthToken requestToken = oauthOperations.fetchRequestToken(generatedUrl, null);
        String authorizeUrl = oauthOperations.buildAuthenticateUrl(requestToken.getValue(), params);
        return authorizeUrl;
    }

    @Override
    public Identity getIdentity(OauthParams params, String oAuthService, String domain) throws UnauthorizedException {

        OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuthToken accessToken = null;
        try {
            accessToken = oauthOperations.exchangeForAccessToken(new AuthorizedRequestToken(new OAuthToken(params.getToken(), secret),
                    params.getVerifier()), null);
        } catch (HttpClientErrorException e) {
            throw new UnauthorizedException("Unable to verify identity with Twitter");
        }
        Connection<Twitter> connection = connectionFactory.createConnection(accessToken);
        if (connection != null && !connection.hasExpired()) {
            Twitter twitter = connection.getApi();
            return identityRepository.findByOauthIdAndDomainAndOauthService(
                    String.valueOf(twitter.userOperations().getUserProfile().getId()), domain, oAuthService);
        }

        throw new UnauthorizedException("Unable to verify identity with Twitter");
    }

    public boolean getIdentity(String userSocialId, String assertion, OauthParams params) {
        OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuthToken accessToken = oauthOperations.exchangeForAccessToken(new AuthorizedRequestToken(
                new OAuthToken(params.getToken(), secret), params.getVerifier()), null);
        Connection<Twitter> connection = connectionFactory.createConnection(accessToken);
        if (connection != null && !connection.hasExpired()) {
            Twitter twitter = connection.getApi();
            return twitter.userOperations().getUserProfile().getId() == Long.valueOf(userSocialId);
        }
        return false;
    }

}
