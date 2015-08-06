package io.corbel.iam.auth.oauthserver.connect;

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;
import org.springframework.social.oauth2.OAuth2Template;

import io.corbel.iam.auth.oauthserver.api.Endpoint;
import io.corbel.iam.auth.oauthserver.api.OAuthServer;
import io.corbel.iam.auth.oauthserver.api.impl.OAuthServerTemplate;

public class OAuthServerServiceProvider extends AbstractOAuth2ServiceProvider<OAuthServer> {

    private final String domainUrl;

    public OAuthServerServiceProvider(String clientId, String clientSecret, String domainUrl) {
        super(getTemplate(clientId, clientSecret, domainUrl));
        this.domainUrl = domainUrl;
    }

    private static OAuth2Template getTemplate(String clientId, String clientSecret, String domainUrl) {
        OAuth2Template oauth2Template = new OAuth2Template(clientId, clientSecret, domainUrl + Endpoint.AUTHORIZE, domainUrl
                + Endpoint.TOKEN);
        oauth2Template.setUseParametersForClientAuthentication(true);
        return oauth2Template;
    }

    @Override
    public OAuthServer getApi(String accessToken) {
        return new OAuthServerTemplate(accessToken, domainUrl);
    }

}
